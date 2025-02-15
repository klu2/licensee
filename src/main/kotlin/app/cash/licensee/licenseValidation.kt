/*
 * Copyright (C) 2021 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.cash.licensee

import java.io.Serializable

internal data class ValidationConfig(
  val allowedIdentifiers: Set<String>,
  val allowedUrls: Set<String>,
  val allowedCoordinates: Map<DependencyCoordinates, String?>,
) : Serializable

internal data class DependencyCoordinates(
  val group: String,
  val artifact: String,
  val version: String,
) : Serializable

internal fun validateArtifacts(
  validationConfig: ValidationConfig,
  artifacts: List<ArtifactDetail>,
): ValidationResults {
  val configResults = mutableListOf<ValidationResult>()
  val artifactResultMap = mutableMapOf<ArtifactDetail, List<ValidationResult>>()

  val unusedAllowedIdentifiers = validationConfig.allowedIdentifiers.toMutableSet()
  val unusedAllowedUrls = validationConfig.allowedUrls.toMutableSet()
  val unusedAllowedCoordinates = validationConfig.allowedCoordinates.keys.toMutableSet()

  for (artifact in artifacts) {
    val artifactResults = mutableListOf<ValidationResult>()
    val dependencyCoordinates =
      DependencyCoordinates(artifact.groupId, artifact.artifactId, artifact.version)

    for (spdxLicense in artifact.spdxLicenses) {
      artifactResults += if (spdxLicense.identifier in validationConfig.allowedIdentifiers) {
        unusedAllowedIdentifiers -= spdxLicense.identifier
        ValidationResult.Info(
          "SPDX identifier '${spdxLicense.identifier}' allowed"
        )
      } else if (spdxLicense.url in validationConfig.allowedUrls) {
        unusedAllowedUrls -= spdxLicense.url
        ValidationResult.Warning(
          "License URL '${spdxLicense.url}' was allowed but could use SPDX identifier '${spdxLicense.identifier}'"
        )
      } else {
        ValidationResult.Error("SPDX identifier '${spdxLicense.identifier}' is NOT allowed")
      }
    }
    for (unknownLicense in artifact.unknownLicenses) {
      artifactResults += if (unknownLicense.url == null) {
        ValidationResult.Error("Unknown license name '${unknownLicense.name}' with no URL is NOT allowed")
      } else if (unknownLicense.url in validationConfig.allowedUrls) {
        unusedAllowedUrls -= unknownLicense.url
        ValidationResult.Info("Unknown license URL '${unknownLicense.url}' allowed")
      } else {
        ValidationResult.Error("Unknown license URL '${unknownLicense.url}' is NOT allowed")
      }
    }
    if (artifact.spdxLicenses.isEmpty() && artifact.unknownLicenses.isEmpty()) {
      artifactResults += ValidationResult.Error("Artifact declares no licenses!")
    }

    if (artifactResults.any { it is ValidationResult.Error }) {
      if (dependencyCoordinates in validationConfig.allowedCoordinates) {
        unusedAllowedCoordinates -= dependencyCoordinates

        artifactResults += ValidationResult.Info(
          buildString {
            append("Coordinate version is allowed")

            val reason = validationConfig.allowedCoordinates[dependencyCoordinates]
            if (reason != null) {
              append(" because ")
              append(reason)
            }
          }
        )

        // Downgrade errors to info.
        artifactResults.forEachIndexed { index, result ->
          if (result is ValidationResult.Error) {
            artifactResults[index] = ValidationResult.Info(result.message)
          }
        }
      } else {
        val candidate = validationConfig.allowedCoordinates.keys
          .firstOrNull { it.group == artifact.groupId && it.artifact == artifact.artifactId }
        if (candidate != null) {
          artifactResults += ValidationResult.Warning(
            "Coordinates match an allowed dependency but version does not match (${candidate.version} != ${artifact.version})"
          )
        }
      }
    }

    artifactResultMap[artifact] = artifactResults
  }

  for (unusedAllowedIdentifier in unusedAllowedIdentifiers) {
    configResults += ValidationResult.Warning("Allowed SPDX identifier '$unusedAllowedIdentifier' is unused")
  }
  for (unusedAllowedUrl in unusedAllowedUrls) {
    configResults += ValidationResult.Warning("Allowed license URL '$unusedAllowedUrl' is unused")
  }
  for (unusedAllowedCoordinate in unusedAllowedCoordinates) {
    configResults += ValidationResult.Warning("Allowed dependency '${unusedAllowedCoordinate.group}:${unusedAllowedCoordinate.artifact}:${unusedAllowedCoordinate.version}' is unused")
  }

  return ValidationResults(configResults, artifactResultMap)
}

internal data class ValidationResults(
  val configResults: List<ValidationResult>,
  val artifactResults: Map<ArtifactDetail, List<ValidationResult>>,
) {
  val containsErrors: Boolean
    get() = configResults.any { it is ValidationResult.Error } ||
      artifactResults.any { it.value.any { it is ValidationResult.Error } }
}

internal sealed class ValidationResult {
  abstract val message: String

  data class Info(override val message: String) : ValidationResult()
  data class Warning(override val message: String) : ValidationResult()
  data class Error(override val message: String) : ValidationResult()
}
