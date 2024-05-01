# osrs-updater
This program attempts to deobfuscate OSRS gamepacks and identify classes, fields and methods.

## Options

| Argument        | Description                                                                                                       |
|-----------------|:------------------------------------------------------------------------------------------------------------------|
| -jar            | Path to jar file to use for single run.                                                                           |
| -base-path      | Base path to look for jars starting with jars in format osrs-REVISION.jar.                                        |
| -start-revision | Starting revision number to check from. The starting revision is considered the source for cross revision checks. |
| -end-revision   | End revision number for cross revision checks.                                                                    |
| --gtf           | Saves renamed gamepack classes to disk.                                                                           |
| --rtf           | Pipes output to disk rather than console.                                                                         |

## Usage

Run deobfuscation and updater for single revision.

```bash
--jar /path-to/osrs-gamepacks/gamepacks/osrs-209.jar
```

Run deobfuscation and updater across multiple revisions.

```bash
-base-path /path-to/osrs-gamepacks/gamepacks/ -start-revision 209 -end-revision 221
```

## Hooks

Hooks are stored under the hooks folder. The format is based on Soxs updater and will change in the future.

## References

[Analysers based on.](https://github.com/Soxs/OSRSUpdater)

[Deobfuscation based on.](https://github.com/archive-runebox/revtools)

[Gamepacks used for analysis.](https://github.com/runetech/osrs-gamepacks)
