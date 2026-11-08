# TODOS

## Deferred from the product review (2026-11-01)

### Photos on completed hikes
- **Why deferred:** not in the brief's MVP; I chose to defer (CEO-4).
- **Context:** journal entries are text-only in this project. Photos would answer the brief's "photos are difficult to find later".
- **When built:** use the Photo Picker (no permission needed), and copy each picked image into app-private storage, because picker URI access does not survive a restart. Delete the copies when the trip is deleted.
- **Depends on:** journal mode (rule 6) being stable.
