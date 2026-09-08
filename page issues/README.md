# KoHs Issue Page

## Run

```bash
npm install
npm start
```

Open `http://localhost:3000`.

## What gets saved

Form fields:

- Category
- Problem description
- Contact (optional)
- Images (optional)

Each submitted report is stored under:

```txt
reports/<ip>/<issue_id>/
```

With:

- `report.json` (all form data + metadata)
- `images/` (optional uploaded images)
