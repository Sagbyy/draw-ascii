package asciidraw

class DrawingSuite extends munit.FunSuite:
  test("horizontal line points"):
    assertEquals(Drawing.linePoints(1, 1, 4, 1), Some(List((1, 1), (2, 1), (3, 1), (4, 1))))

  test("vertical line points"):
    assertEquals(Drawing.linePoints(2, 0, 2, 2), Some(List((2, 0), (2, 1), (2, 2))))

  test("line points in reverse order"):
    assertEquals(Drawing.linePoints(4, 1, 1, 1), Some(List((1, 1), (2, 1), (3, 1), (4, 1))))

  test("single point line"):
    assertEquals(Drawing.linePoints(3, 3, 3, 3), Some(List((3, 3))))

  test("diagonal line is not supported"):
    assertEquals(Drawing.linePoints(0, 0, 3, 3), None)
