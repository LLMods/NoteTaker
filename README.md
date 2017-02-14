# SpigotMod-Notes

A simple plugin for a Spigot server that allows users to save small notes via the command
line. Simple CRUD on those notes.

Currently supports SQLite (not in memory), MySQL, and PostgreSQL.

## How does it work?

Use `/note` to work with the command. Basic CRUD operations are
offered:

```
/note create [note_text] # Creates a note with the given text
/note list               # Shows all created notes of given user
/note read [note_id]     # Shows full text of note requested
/note delete [note_id]   # Deletes the note matching the given ID
```

## How do I install it?

1. `git clone` this repo
2. `cd` into the repo and `mvn package`
3. `mv *.jar /your/server/path/plugins`
4. Start your server, or issue the command `reload` on the server
5. Modify the config.yml file according to your DB
6. `reload` one more time

## What can I do to help out?

* You can contribute code by simply forking the repo, adding your
fix/feature, and then submit a pull request
* You can write some test cases
* You can submit a bug report by creating a new issue