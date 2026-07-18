package asciidraw

class ParserSuite extends munit.FunSuite:
  test("parse canvas"):
    assertEquals(Parser.parse("canvas 10 5"), Right(Command.CreateCanvas(10, 5)))

  test("parse point"):
    assertEquals(Parser.parse("point 3 2"), Right(Command.DrawPoint(3, 2)))

  test("parse line"):
    assertEquals(Parser.parse("line 1 1 6 1"), Right(Command.DrawLine(1, 1, 6, 1)))

  test("parse rect"):
    assertEquals(Parser.parse("rect 2 1 5 3"), Right(Command.DrawRect(2, 1, 5, 3)))

  test("parse fill"):
    assertEquals(Parser.parse("fill 3 2 o"), Right(Command.Fill(3, 2, 'o')))

  test("parse setchar"):
    assertEquals(Parser.parse("setchar x"), Right(Command.SetChar('x')))

  test("parse clear"):
    assertEquals(Parser.parse("clear"), Right(Command.Clear))

  test("parse undo and redo"):
    assertEquals(Parser.parse("undo"), Right(Command.Undo))
    assertEquals(Parser.parse("redo"), Right(Command.Redo))
    assertEquals(Parser.parse("undo 1"), Left(AppError.InvalidArgumentCount("undo")))

  test("parse rejects invalid char"):
    assertEquals(Parser.parse("setchar ab"), Left(AppError.InvalidChar("ab")))
    assertEquals(Parser.parse("fill 3 2 ab"), Left(AppError.InvalidChar("ab")))

  test("parse render"):
    assertEquals(Parser.parse("render"), Right(Command.Render))

  test("parse quit and exit"):
    assertEquals(Parser.parse("quit"), Right(Command.Quit))
    assertEquals(Parser.parse("exit"), Right(Command.Quit))

  test("parse ignores extra spaces"):
    assertEquals(Parser.parse("  point   3  2 "), Right(Command.DrawPoint(3, 2)))

  test("parse rejects unknown command"):
    assertEquals(Parser.parse("circle 1 2"), Left(AppError.UnknownCommand("circle")))

  test("parse rejects wrong argument count"):
    assertEquals(Parser.parse("canvas 10"), Left(AppError.InvalidArgumentCount("canvas")))
    assertEquals(Parser.parse("point 1 2 3"), Left(AppError.InvalidArgumentCount("point")))
    assertEquals(Parser.parse("line 1 2"), Left(AppError.InvalidArgumentCount("line")))
    assertEquals(Parser.parse("rect 1 2"), Left(AppError.InvalidArgumentCount("rect")))
    assertEquals(Parser.parse("fill 3 2"), Left(AppError.InvalidArgumentCount("fill")))
    assertEquals(Parser.parse("setchar"), Left(AppError.InvalidArgumentCount("setchar")))
    assertEquals(Parser.parse("clear 1"), Left(AppError.InvalidArgumentCount("clear")))
    assertEquals(Parser.parse("render 1"), Left(AppError.InvalidArgumentCount("render")))
    assertEquals(Parser.parse("quit 1"), Left(AppError.InvalidArgumentCount("quit")))

  test("parse rejects non-numeric arguments"):
    assertEquals(Parser.parse("point a 2"), Left(AppError.InvalidNumber("a")))
