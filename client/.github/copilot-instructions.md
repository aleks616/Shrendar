## Strict workspace boundary

The workspace root is `/Users/aleks/IdeaProjects/Shrendar/client`.

- Only read, search, create, or modify files under this workspace.
- Never access `/Users/aleks/IdeaProjects/Shrendar/backend`.
- Never access the parent `Shrendar` directory or sibling projects.
- Do not inspect server files, backend files, secrets, or files outside the workspace.
- If completing a task appears to require an out-of-scope file, stop and ask first.
- Prefer the files explicitly tagged in the user request.

## Basic rules
- Do exactly what the user asks for, and do not try to add extra "features" or "improvements" unless explicitly requested.
- If code to base on is provided, closely follow the style, structure, and patterns of the provided code.
- If you're writing a version of an existing view for another platform, follow the same structure and patterns as the original view, combining it with patterns for the platform.
- If you are asked to write a new view, follow the same structure and patterns as existing views for that platform.
- Do not actually run the app
- If you're listing changes, don't list "keep" etc. as a change, only list actual changes.
- Do not write code in chat unless requested. If you can't edit and the prompt is "edit" simply say that you can't edit the code and ask to change the mode to "agent" so you can edit the code.
