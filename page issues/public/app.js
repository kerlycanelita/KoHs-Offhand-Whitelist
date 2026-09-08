const form = document.getElementById("issueForm");
const submitBtn = document.getElementById("submitBtn");
const statusEl = document.getElementById("status");
const imagesInput = document.getElementById("images");
const imagesPreview = document.getElementById("imagesPreview");
const tabButtons = Array.from(document.querySelectorAll(".tab-btn"));
const tabPanels = Array.from(document.querySelectorAll(".tab-panel"));
const nextButtons = Array.from(document.querySelectorAll(".next-btn"));
const prevButtons = Array.from(document.querySelectorAll(".prev-btn"));

function setStatus(message, type = "") {
  statusEl.textContent = message;
  statusEl.classList.remove("ok", "error");
  if (type) {
    statusEl.classList.add(type);
  }
}

function formatBytes(bytes) {
  if (!Number.isFinite(bytes)) return "0 B";
  if (bytes < 1024) return `${bytes} B`;
  const kb = bytes / 1024;
  if (kb < 1024) return `${kb.toFixed(1)} KB`;
  return `${(kb / 1024).toFixed(2)} MB`;
}

function showTab(index) {
  const safeIndex = Math.max(0, Math.min(index, tabPanels.length - 1));
  tabButtons.forEach((button, i) => {
    button.classList.toggle("active", i === safeIndex);
  });
  tabPanels.forEach((panel, i) => {
    panel.classList.toggle("active", i === safeIndex);
  });
}

tabButtons.forEach((button) => {
  button.addEventListener("click", () => {
    const index = Number(button.dataset.tab);
    showTab(index);
  });
});

nextButtons.forEach((button) => {
  button.addEventListener("click", () => {
    const index = Number(button.dataset.next);
    showTab(index);
  });
});

prevButtons.forEach((button) => {
  button.addEventListener("click", () => {
    const index = Number(button.dataset.prev);
    showTab(index);
  });
});

imagesInput.addEventListener("change", () => {
  const files = Array.from(imagesInput.files || []);
  if (!files.length) {
    imagesPreview.textContent = "No images selected.";
    return;
  }
  const lines = files.slice(0, 6).map((file, idx) => `${idx + 1}. ${file.name} (${formatBytes(file.size)})`);
  imagesPreview.textContent = lines.join(" | ");
});

form.addEventListener("submit", async (event) => {
  event.preventDefault();
  setStatus("");

  const files = Array.from(imagesInput.files || []);
  if (files.length > 6) {
    setStatus("You can upload up to 6 images.", "error");
    showTab(3);
    return;
  }

  const issueType = String(form.issueType?.value || "").trim();
  const description = String(form.description?.value || "").trim();
  if (!issueType) {
    setStatus("Please select a category.", "error");
    showTab(0);
    return;
  }
  if (!description) {
    setStatus("Please add a problem description.", "error");
    showTab(1);
    return;
  }

  submitBtn.disabled = true;
  submitBtn.textContent = "Submitting...";

  try {
    const formData = new FormData(form);
    const response = await fetch("/api/issues", {
      method: "POST",
      body: formData
    });

    const data = await response.json();
    if (!response.ok || !data.ok) {
      setStatus(data.message || "Could not submit your report.", "error");
      return;
    }

    setStatus(`Issue submitted. ID: ${data.issueId}`, "ok");
    form.reset();
    imagesPreview.textContent = "No images selected.";
    showTab(0);
  } catch (_error) {
    setStatus("Network error while submitting the report.", "error");
  } finally {
    submitBtn.disabled = false;
    submitBtn.textContent = "Submit Issue";
  }
});

imagesPreview.textContent = "No images selected.";
