package asciidraw

class CanvasSuite extends munit.FunSuite:
  test("create accepts positive dimensions"):
    assertEquals(Canvas.create(5, 3).map(c => (c.width, c.height)), Right((5, 3)))

  test("create rejects non-positive dimensions"):
    assertEquals(Canvas.create(0, 3), Left(AppError.InvalidDimensions(0, 3)))
    assertEquals(Canvas.create(5, -1), Left(AppError.InvalidDimensions(5, -1)))

  test("render empty canvas"):
    assertEquals(Canvas.create(5, 3).map(_.render), Right(".....\n.....\n....."))

  test("contains checks bounds"):
    val canvas = Canvas(5, 3, Map.empty)
    assert(canvas.contains(0, 0))
    assert(canvas.contains(4, 2))
    assert(!canvas.contains(5, 0))
    assert(!canvas.contains(0, -1))

  test("withPixel draws a char"):
    val canvas = Canvas(3, 2, Map.empty).withPixel(1, 1, '*')
    assertEquals(canvas.render, "...\n.*.")

  test("withPixels draws all points"):
    val canvas = Canvas(3, 2, Map.empty).withPixels(List((0, 0), (1, 0), (2, 0)), '#')
    assertEquals(canvas.render, "###\n...")
