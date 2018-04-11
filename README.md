# sbt-gpg

Simple and secure artifact signing for sbt.

This sbt plugin aims to make artifact signing simple and
unobtrusive. It is guided by two core ideas:

1. easy configuration with sane defaults
2. use of standard cryptography tools (gpg)

The motivation is that these priniciple are both essential in
promoting secure builds.

## Highlights

- Uses the system command `gpg` to do all operations. *This enables
  advanced features such as use of smartcards or cutting-edge
  ciphers.*

- Hooks into the `publish` and `publishLocal` tasks. *All artrifacts
  will be signed; there is no need to run a separate `publishSigned`
  task.*

- Unobtrusive configuration. *Key selection can be done through sbt's
  `credentials` mechanism, thus enabling global configuration without
  the need of adding a global plugin.*

- Works out-of-the-box. *Publishing falls back to unsigned artifacts
  in case key material cannot be found, after emitting an explicit
  warning.*
  
## Requirements

- sbt version >= 1.0.0
- gpg installed on user's machine (this requirement won't get in the
  way of a user's productivity; missing gpg will simply disable the
  functionality provided by this plugin)

## Getting started
```scala
addSbtPlugin("io.crashbox" % "sbt-gpg" % "<latest_tag>")
```
Copy the above snippet to an sbt configuration file. E.g.

- `project/plugins.sbt` to enable the plugin on a per-project basis
- `~/.sbt/1.0/plugins/gpg.sbt` to enable the plugin globally

The autoplugin "SbtGpg" will be enabled and modify the `publish` and
`publishLocal` tasks to include signatures of all published artifacts.

## Configuration

### Signing key
By default, all signing operations will use `gpg`'s default key. A
specific key can be used by setting sbt `Credentials` for the host
"gpg".

```scala
credentials += Credentials(
  "GnuPG Key ID",
  "gpg",
  "4E7DA7B5A0F86992D6EB3F514601878662E33372",
  "ignored"
)
```

The user name (3rd field) will determine the key to use and can be any
valid key id, fingerprint, email or user accepted by gpg.

### Other settings
Check out the [autoplugin definition](src/main/scala/SbtGpg.scala) for
an exhaustive list of settings and tasks that can be customized.
