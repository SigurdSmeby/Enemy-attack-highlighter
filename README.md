# Enemy Attack Highlighter

Highlights NPCs that you tag.

## Features
- Tags NPCs when your own hitsplats land (including cannon and `0` misses).
- Configurable tag timeout in seconds (default `10`).
- Set timeout to `0` to keep tags until death/despawn.
- Optional multicombat-only mode.
- Optional whitelist mode with CSV NPC names (`goblin, dust devil, ...`).
- Optional out-of-range expiry (`range tiles` + `out of range seconds`).
- Automatic re-tag cooldown (`tag duration / 2`, or lock while tagged when duration is `0`).
- Optional debug labels showing tagged NPC names.
- Configurable highlight mode: `outline`, `tile`, or `both`.
- Configurable highlight color.

## Development
- Java 11
- RuneLite dependency: `latest.release`
- Run client from IDE using the `run` gradle task.
