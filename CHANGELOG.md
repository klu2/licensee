# Change Log

## [Unreleased]

## [1.3.0] - 2021-10-26

**Fixed**

- Support Kotlin multiplatform projects with the 'application' plugin applied.


## [1.3.0] - 2021-10-26

**Added**

- `violationAction` build DSL which allows you to choose whether to fail, log, or ignore license
  validation problems.
- New fallback URLs for popular licenses such as MIT, Apache 2, LGPL, GPL, BSD, and EPL.

**Fixed**

- Ignore flat-dir repositories which contain no artifact metadata.
- Support Kotlin multiplatform projects whose JVM target uses `withJava()`.


## [1.2.0] - 2021-07-27

**Added**

 - If the license information in a Maven POM is missing a URL, fallback to matching the name against the SPDX identifier list.


## [1.1.0] - 2021-06-25

**Added**

 - Include SCM URL in the JSON output if available from an artifacts POM.

**Fixed**

 - Support older versions of Gradle because they leak ancient versions of the Kotlin stdlib onto the plugin classpath.


## [1.0.2] - 2021-06-09

**Changed**

 - Report the offending project when the plugin fails to apply due to a missing sibling language/platform plugin.


## [1.0.1] - 2021-06-08

**Changed**

 - Include Gradle-internal resolution exception in `--info` log when a POM fails to resolve.
 - Introduce determinism when dealing with multiple license identifiers which use the same license URL. For now, the shortest identifier is selected rather than relying on order of the SPDX license JSON.


## [1.0.0] - 2021-05-21

Initial release.



[Unreleased]: https://github.com/cashapp/licensee/compare/1.3.1...HEAD
[1.3.1]: https://github.com/cashapp/licensee/releases/tag/1.3.1
[1.3.0]: https://github.com/cashapp/licensee/releases/tag/1.3.0
[1.2.0]: https://github.com/cashapp/licensee/releases/tag/1.2.0
[1.1.0]: https://github.com/cashapp/licensee/releases/tag/1.1.0
[1.0.2]: https://github.com/cashapp/licensee/releases/tag/1.0.2
[1.0.1]: https://github.com/cashapp/licensee/releases/tag/1.0.1
[1.0.0]: https://github.com/cashapp/licensee/releases/tag/1.0.0
