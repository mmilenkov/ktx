package ktx.box2d

import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType
import com.badlogic.gdx.physics.box2d.Fixture
import com.badlogic.gdx.physics.box2d.World

/**
 * [World] factory function.
 * @param gravity world's gravity applied to bodies on each step. Defaults to no gravity (0f, 0f).
 * @param allowSleep if true, inactive bodies will not be simulated. Improves performance. Defaults to true.
 * @return a new [World] instance with given parameters.
 */
fun createWorld(gravity: Vector2 = Vector2.Zero, allowSleep: Boolean = true) = World(gravity, allowSleep)

/**
 * Type-safe [Body] building DSL.
 * @param type [BodyType] of the constructed [Body]. Matches LibGDX default of [BodyType.StaticBody].
 * @param init inlined. Invoked on a [BodyDefinition] instance, which provides access to [Body] properties, as well as
 *    fixture building DSL.
 * @return a fully constructed [Body] instance with all defined fixtures.
 * @see BodyDefinition
 * @see FixtureDefinition
 */
inline fun World.body(type: BodyType = BodyType.StaticBody, init: BodyDefinition.() -> Unit): Body {
  val bodyDefinition = BodyDefinition()
  bodyDefinition.type = type
  bodyDefinition.init()
  return create(bodyDefinition)
}

/**
 * Handles additional building properties provided by [BodyDefinition] and [FixtureDefinition]. Prefer this method
 * over [World.createBody] when using [BodyDefinition] directly.
 * @param bodyDefinition stores [Body] properties and optional [Fixture] definitions.
 * @return a fully constructed [Body] instance with all defined fixtures.
 * @see BodyDefinition
 * @see FixtureDefinition
 * @see body
 */
fun World.create(bodyDefinition: BodyDefinition): Body {
  val body = createBody(bodyDefinition)
  body.userData = bodyDefinition.userData
  for (fixtureDefinition in bodyDefinition.fixtureDefinitions) {
    val fixture = body.createFixture(fixtureDefinition)
    fixture.userData = fixtureDefinition.userData
    fixtureDefinition.creationCallback?.let { it(fixture) }
  }
  bodyDefinition.creationCallback?.let { it(body) }
  return body
}

/**
 * Roughly matches Earth gravity of 9.80665 m/s^2. Moves bodies down on the Y axis.
 *
 * Note that [Vector2] class is mutable, so this vector can be modified. Use this property in read-only mode.
 *
 * Usage example:
 * val world = createWorld(gravity = earthGravity)
 * @see createWorld
 */
val earthGravity = Vector2(0f, -9.8f)

/**
 * Callback lambda for ray-casts. This lambda is called when a ray-cast hits a fixture.
 *
 * The lambda accepts these parameters:
 * - `[Fixture]`, the fixture hit by the ray
 * - `[Vector2]`, the point of initial intersection
 * - `[Vector2]`, the normal vector at the point of intersection
 * - `[Float]`, the fraction of the distance from `start` to `end` that the intersection point is at
 *
 * The lambda should return:
 * - `-1f`, ignore this fixture and continue
 * - `0f`, terminate the ray cast
 * - a fraction, clip the length of the ray to this point
 * - `1f`, don't clip the ray and continue
 *
 * Can be used in place of [com.badlogic.gdx.physics.box2d.RayCastCallback] via Kotlin SAM conversion.
 */
typealias KtxRayCastCallback = (Fixture, Vector2, Vector2, Float) -> Float

/**
 * Return value for [KtxRayCastCallback].
 *
 * Indicates to ignore the hit fixture and continue.
 */
const val IGNORE_FIXTURE = -1f
/**
 * Return value for [KtxRayCastCallback].
 *
 * Indicates to terminate the ray cast
 */
const val TERMINATE_RAY_CAST = 0f
/**
 * Return value for [KtxRayCastCallback].
 *
 * Indicates to not clip the ray and continue
 */
const val CONTINUE_RAY_CAST = 1f

/**
 * Ray-cast the world for all fixtures in the path of the ray.
 *
 * The ray-cast ignores shapes that contain the starting point.
 *
 * @param start the ray starting point
 * @param end the ray ending point
 * @param callback a user implemented callback. This is called for every fixture hit.
 */
fun World.rayCast(start: Vector2, end: Vector2, callback: KtxRayCastCallback) {
  rayCast(callback, start, end)
}

/**
 * Ray-cast the world for all fixtures in the path of the ray.
 *
 * The ray-cast ignores shapes that contain the starting point.
 *
 * @param startX the ray starting point X
 * @param startY the ray starting point Y
 * @param endX the ray ending point X
 * @param endY the ray ending point Y
 * @param callback a user implemented callback. This is called for every fixture hit.
 */
fun World.rayCast(startX: Float, startY: Float, endX: Float, endY: Float, callback: KtxRayCastCallback) {
  rayCast(callback, startX, startY, endX, endY)
}
