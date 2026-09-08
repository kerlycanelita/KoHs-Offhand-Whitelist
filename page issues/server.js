const crypto = require("crypto");
const fs = require("fs/promises");
const path = require("path");

const express = require("express");
const multer = require("multer");

const app = express();
const port = Number(process.env.PORT || 3000);

const rootDir = __dirname;
const publicDir = path.join(rootDir, "public");
const reportsDir = path.join(rootDir, "reports");

const upload = multer({
  storage: multer.memoryStorage(),
  limits: {
    fileSize: 10 * 1024 * 1024,
    files: 6
  }
});

app.use(express.static(publicDir));
app.use(express.urlencoded({ extended: true }));

function normalizeClientIp(ip) {
  if (!ip) {
    return "unknown";
  }
  if (ip.startsWith("::ffff:")) {
    return ip.slice(7);
  }
  if (ip === "::1") {
    return "127.0.0.1";
  }
  return ip;
}

function getClientIp(req) {
  const forwarded = req.headers["x-forwarded-for"];
  if (typeof forwarded === "string" && forwarded.trim()) {
    return normalizeClientIp(forwarded.split(",")[0].trim());
  }
  return normalizeClientIp(req.socket?.remoteAddress || req.ip || "unknown");
}

function sanitizeSegment(value) {
  return String(value || "unknown")
    .trim()
    .replace(/[^a-zA-Z0-9._-]/g, "_")
    .replace(/^_+|_+$/g, "")
    .slice(0, 120) || "unknown";
}

function createIssueId() {
  const stamp = new Date().toISOString().replace(/[:.]/g, "-");
  const random = crypto.randomBytes(3).toString("hex");
  return `${stamp}_${random}`;
}

function imageExtension(file) {
  const extFromName = path.extname(file.originalname || "").toLowerCase();
  if (extFromName) {
    return extFromName;
  }
  if (file.mimetype === "image/png") return ".png";
  if (file.mimetype === "image/jpeg") return ".jpg";
  if (file.mimetype === "image/webp") return ".webp";
  if (file.mimetype === "image/gif") return ".gif";
  return ".bin";
}

app.get("/api/health", (_req, res) => {
  res.json({ ok: true });
});

app.post("/api/issues", upload.array("images", 6), async (req, res) => {
  const body = req.body || {};
  const issueType = String(body.issueType || "").trim();
  const description = String(body.description || "").trim();

  if (!issueType) {
    return res.status(400).json({ ok: false, message: "Issue type is required." });
  }
  if (!description) {
    return res.status(400).json({ ok: false, message: "Description is required." });
  }

  try {
    const clientIpRaw = getClientIp(req);
    const clientIpFolder = sanitizeSegment(clientIpRaw);
    const issueId = createIssueId();
    const issueDir = path.join(reportsDir, clientIpFolder, issueId);
    const imagesDir = path.join(issueDir, "images");
    const files = Array.isArray(req.files) ? req.files : [];

    await fs.mkdir(imagesDir, { recursive: true });

    const savedImages = [];
    for (let i = 0; i < files.length; i += 1) {
      const file = files[i];
      const baseName = sanitizeSegment(path.parse(file.originalname || "image").name).slice(0, 64);
      const extension = imageExtension(file);
      const fileName = `${String(i + 1).padStart(2, "0")}_${baseName || "image"}${extension}`;
      const destination = path.join(imagesDir, fileName);
      await fs.writeFile(destination, file.buffer);

      savedImages.push({
        fileName,
        originalName: file.originalname || "image",
        mimeType: file.mimetype,
        bytes: file.size
      });
    }

    const payload = {
      issueId,
      createdAt: new Date().toISOString(),
      ip: clientIpRaw,
      ipFolder: clientIpFolder,
      userAgent: String(req.headers["user-agent"] || ""),
      issueType,
      description,
      contact: String(body.contact || "").trim(),
      imageCount: savedImages.length,
      images: savedImages
    };

    await fs.writeFile(path.join(issueDir, "report.json"), JSON.stringify(payload, null, 2), "utf8");

    return res.status(201).json({
      ok: true,
      message: "Issue submitted successfully.",
      issueId,
      storagePath: path.relative(rootDir, issueDir).replace(/\\/g, "/")
    });
  } catch (error) {
    return res.status(500).json({
      ok: false,
      message: "Could not save the report.",
      error: error.message
    });
  }
});

app.use((err, _req, res, _next) => {
  if (err?.code === "LIMIT_FILE_SIZE") {
    return res.status(400).json({ ok: false, message: "One or more images are too large (max 10MB)." });
  }
  if (err?.code === "LIMIT_FILE_COUNT") {
    return res.status(400).json({ ok: false, message: "Too many images (max 6)." });
  }
  return res.status(500).json({ ok: false, message: "Unexpected server error." });
});

fs.mkdir(reportsDir, { recursive: true })
  .then(() => {
    app.listen(port, () => {
      // eslint-disable-next-line no-console
      console.log(`Issue page running on http://localhost:${port}`);
    });
  })
  .catch((error) => {
    // eslint-disable-next-line no-console
    console.error("Could not create reports directory:", error);
    process.exit(1);
  });
