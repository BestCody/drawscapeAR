package com.example.eureka
import com.google.ar.core.Anchor
import com.google.ar.core.Session

class CloudAnchorManager(private val session: Session) {

    var onAnchorHosted: ((cloudAnchorId: String) -> Unit)? = null
    var onAnchorResolved: ((anchor: Anchor) -> Unit)? = null
    var onError: ((message: String) -> Unit)? = null

    private var cloudAnchor: Anchor? = null

    fun hostAnchor(anchor: Anchor) {
        cloudAnchor = session.hostCloudAnchorWithTtl(anchor, /* ttlDays = */ 1)
    }

    /** Call every frame while hosting (inside onSessionUpdated). */
    fun checkHostingStatus() {
        val anchor = cloudAnchor ?: return
        when (anchor.cloudAnchorState) {
            Anchor.CloudAnchorState.SUCCESS -> {
                onAnchorHosted?.invoke(anchor.cloudAnchorId)
                cloudAnchor = null
            }
            Anchor.CloudAnchorState.ERROR_NOT_AUTHORIZED ->
                onError?.invoke("API key not authorized for Cloud Anchors")
            Anchor.CloudAnchorState.ERROR_SERVICE_UNAVAILABLE ->
                onError?.invoke("Cloud Anchor service unavailable")
            else -> { /* Still processing */ }
        }
    }

    fun resolveAnchor(cloudAnchorId: String) {
        cloudAnchor = session.resolveCloudAnchor(cloudAnchorId)
    }

    /** Call every frame while resolving. */
    fun checkResolvingStatus() {
        val anchor = cloudAnchor ?: return
        when (anchor.cloudAnchorState) {
            Anchor.CloudAnchorState.SUCCESS -> {
                onAnchorResolved?.invoke(anchor)
                cloudAnchor = null
            }
            Anchor.CloudAnchorState.ERROR_RESOLVE_SDK_VERSION_TOO_OLD ->
                onError?.invoke("Update ARCore SDK")
            else -> { /* Still resolving */ }
        }
    }
}