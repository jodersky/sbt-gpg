[![Build Status](https://travis-ci.org/jodersky/sbt-gpg.svg?branch=master)](https://travis-ci.org/jodersky/sbt-gpg)
[![Scaladex](https://index.scala-lang.org/jodersky/sbt-gpg/latest.svg)](https://index.scala-lang.org/jodersky/sbt-gpg)

# sbt-gpg

Simple and secure artifact signing for sbt.

This sbt plugin aims to make artifact signing simple and
unobtrusive. It is guided by two core ideas:

1. easy configuration with sane defaults
2. use of standard cryptography tools (gpg)

The motivation is that these principles are both essential for
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

That's it! The autoplugin "SbtGpg" will now be enabled for the given
project(s). It will modify the `publish` and `publishLocal` tasks to
always include signatures of all published artifacts.

The default configuration will pick up local GPG settings. See the
next section to find out how to customize the plugin.

## Configuration

### Signing key
By default, all signing operations will use `gpg`'s default key. A
specific key can be used by setting sbt `Credentials` for the host
"gpg".

```scala
credentials += Credentials(
  "GnuPG Key ID",
  "gpg",
  "4E7DA7B5A0F86992D6EB3F514601878662E33372", // key identifier
  "ignored" // passwords are supplied by pinentry
)
```

The user name (3rd field, "key identifier" in the snippet above) will
determine the key to use and can be any valid key id, fingerprint,
email or user accepted by GPG.

### Other settings
Check out the [autoplugin definition](src/main/scala/SbtGpg.scala) for
an exhaustive list of settings and tasks that can be customized.

## Securely Publishing Locally

Some projects use the "release from the maintainer's laptop" strategy. There's nothing *specifically* wrong with this strategy from a security standpoint, so long as the maintainer in question practices good security hygiene and protects their signing key appropriately (hint: *strongly* consider a YubiKey or similar if you maintain OSS projects; it's quite cheap and solves a major security vulnerability).

This plugin is well-optimized for this publication strategy, and in fact it is the secure default! You don't need to do anything to publish using a local key. Simply invoke `publish` (as described above), securely unlock your key when prompted by `pinentry`, and all of the signing and key management will be handled for you.

## Securely Publishing in Travis (and other CI)

*Note: These instructions are written in terms of Travis, since it is probably the most common CI server in use today, but they are easily applicable to any which supports secure key management.*

It is very common to configure your CI server (Travis or otherwise) to perform the artifact signing and publication tasks. Conventionally, builds generally rely upon GPG's key encryption functionality to apply a decryption password to the key. The (password-protected) signing key is then checked in with the source code while the decryption password is encrypted and managed by the CI server. At build-time, this password is decrypted and injected into the build using an environment variable, closing the loop and allowing the CI server to securely decrypt the signing key and publish the artifacts.

This scheme works well when SBT manages signing key passwords and decryption (as in sbt-pgp). It works quite poorly when securely delegating to `pinentry`, as is the case with this plugin.

The solution is to *not* password-protect the CI signing key and instead encrypt it explicitly using `openssl`. To start with, you should have your CI signing key in your local GPG keyring. Let's assume this key has a signature of `1234ABCD`. Run the following commands within your project root:

```bash
$ gpg --export-secret-keys -a 1234ABCD > key.asc
$ travis encrypt_file key.asc --add
$ rm key.asc
$ git add key.asc.enc
$ git commit
```

Replace `travis encrypt_file` with whatever mechanism is required to securely encrypt files for your CI solution. You may omit the `--add` switch and manually modify your `.travis.yml` if you prefer. Travis' file encryption documentation is [here](https://docs.travis-ci.com/user/encrypting-files/).

These steps handle securely materializing a plain-text (*not* password protected!) secret key on your CI server. The only remaining task is to make it available to `gpg` on your CI so that it can be picked up by sbt-gpg. If using Travis, add the following to your `.travis.yml`:

```yaml
before_script: gpg --import key.asc
```

### Best Practices

Do **NOT** use your personal GPG key for CI signing! Ever. Your personal private key should never leave your laptop. In fact, it probably shouldn't be on your laptop at all. Strongly consider YubiKey or similar. Never, *ever* enter the decryption password (or smartcard PIN) for your private key into anything other than `pinentry` on your local machine. If you're using MacGPG (which you should be, if using macOS), this dialog will look similar to the following:

![macgpg-pinentry](https://i.imgur.com/ciol75g.png)

If you are using your CI server to sign artifacts, your CI server should have *its own* public/private key pair, generated by you (or someone else on your team). You probably also want to sign that CI key with your own key, establishing a chain of trust (assuming the CI signing key has ID `1234AVCD`):

```bash
$ gpg --sign-key 1234ABCD
$ gpg --send-key 1234ABCD
```

This is *not* the same as using your personal key for CI signing!
