package asciidraw

class InterpreterSuite extends munit.FunSuite:
  private val canvas = Canvas.create(10, 5).toOption

  private def renderAfter(commands: List[Command]): Either[AppError, Option[String]] =
    commands
      .foldLeft(Right(Interpreter.Result(None, None)): Either[AppError, Interpreter.Result])(
        (state, command) => state.flatMap(result => Interpreter.interpret(result.canvas, command))
      )
      .map(_.output)

  test("create canvas"):
    val result = Interpreter.interpret(None, Command.CreateCanvas(10, 5))
    assertEquals(result.map(_.canvas.map(c => (c.width, c.height))), Right(Some((10, 5))))

  test("create canvas with invalid dimensions"):
    val result = Interpreter.interpret(None, Command.CreateCanvas(0, 5))
    assertEquals(result, Left(AppError.InvalidDimensions(0, 5)))

  test("draw before canvas creation"):
    assertEquals(Interpreter.interpret(None, Command.DrawPoint(1, 1)), Left(AppError.NoCanvas))
    assertEquals(Interpreter.interpret(None, Command.DrawLine(1, 1, 4, 1)), Left(AppError.NoCanvas))

  test("render before canvas creation"):
    assertEquals(Interpreter.interpret(None, Command.Render), Left(AppError.NoCanvas))

  test("draw point"):
    val result = Interpreter.interpret(canvas, Command.DrawPoint(1, 1))
    assertEquals(result.map(_.canvas.map(_.charAt(1, 1))), Right(Some('*')))

  test("draw point out of bounds"):
    val result = Interpreter.interpret(canvas, Command.DrawPoint(20, 3))
    assertEquals(result, Left(AppError.OutOfBounds(20, 3)))

  test("draw horizontal line"):
    val rendered = renderAfter(List(Command.CreateCanvas(5, 3), Command.DrawLine(1, 1, 4, 1), Command.Render))
    assertEquals(rendered, Right(Some(".....\n.####\n.....")))

  test("draw vertical line"):
    val rendered = renderAfter(List(Command.CreateCanvas(5, 3), Command.DrawLine(2, 0, 2, 2), Command.Render))
    assertEquals(rendered, Right(Some("..#..\n..#..\n..#..")))

  test("draw diagonal line is rejected"):
    val result = Interpreter.interpret(canvas, Command.DrawLine(0, 0, 3, 3))
    assertEquals(result, Left(AppError.DiagonalLine))

  test("draw line out of bounds"):
    val result = Interpreter.interpret(canvas, Command.DrawLine(8, 1, 12, 1))
    assertEquals(result, Left(AppError.OutOfBounds(10, 1)))

  test("draw rect outline"):
    val rendered = renderAfter(List(Command.CreateCanvas(6, 4), Command.DrawRect(1, 1, 3, 2), Command.Render))
    assertEquals(rendered, Right(Some("......\n.###..\n.###..\n......")))

  test("draw rect with hollow center"):
    val rendered = renderAfter(List(Command.CreateCanvas(6, 5), Command.DrawRect(1, 1, 4, 3), Command.Render))
    assertEquals(rendered, Right(Some("......\n.####.\n.#..#.\n.####.\n......")))

  test("draw rect before canvas creation"):
    assertEquals(Interpreter.interpret(None, Command.DrawRect(1, 1, 3, 2)), Left(AppError.NoCanvas))

  test("draw rect with invalid dimensions"):
    val result = Interpreter.interpret(canvas, Command.DrawRect(1, 1, 0, 2))
    assertEquals(result, Left(AppError.InvalidDimensions(0, 2)))

  test("draw rect out of bounds"):
    val result = Interpreter.interpret(canvas, Command.DrawRect(7, 1, 5, 2))
    assertEquals(result, Left(AppError.OutOfBounds(10, 1)))

  test("render outputs the grid"):
    val rendered = renderAfter(List(Command.CreateCanvas(5, 3), Command.Render))
    assertEquals(rendered, Right(Some(".....\n.....\n.....")))

  test("quit keeps state and produces no output"):
    assertEquals(Interpreter.interpret(canvas, Command.Quit), Right(Interpreter.Result(canvas, None)))
