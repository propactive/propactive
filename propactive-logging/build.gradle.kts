val isDevVersion = "$version" == "DEV-SNAPSHOT"
val isSemVersioned = "$version".matches(Regex("[0-9]+\\.[0-9]+\\.[0-9]+.*?"))
val isVersionedSnapshot = isSemVersioned && "$version".endsWith("-SNAPSHOT")
val isVersionedRelease = isSemVersioned && isVersionedSnapshot.not()

dependencies { implementation(libs.kotlin.reflect) }
