package asciidraw

class InterpreterSuite extends munit.FunSuite:
  private val session = Session(Canvas.create(10, 5).toOption, None)

  private def interpretAll(commands: List[Command]): Either[AppError, Interpreter.Result] =
    commands.foldLeft(Right(Interpreter.Result(Session.initial, None)): Either[AppError, Interpreter.Result])(
      (state, command) => state.flatMap(result => Interpreter.interpret(result.session, command))
    )

  private def renderAfter(commands: List[Command]): Either[AppError, Option[String]] =
    interpretAll(commands).map(_.output)

  test("create canvas"):
    val result = Interpreter.interpret(Session.initial, Command.CreateCanvas(10, 5))
    assertEquals(result.map(_.session.canvas.map(c => (c.width, c.height))), Right(Some((10, 5))))

  test("create canvas with invalid dimensions"):
    val result = Interpreter.interpret(Session.initial, Command.CreateCanvas(0, 5))
    assertEquals(result, Left(AppError.InvalidDimensions(0, 5)))

  test("draw before canvas creation"):
    assertEquals(Interpreter.interpret(Session.initial, Command.DrawPoint(1, 1)), Left(AppError.NoCanvas))
    assertEquals(Interpreter.interpret(Session.initial, Command.DrawLine(1, 1, 4, 1)), Left(AppError.NoCanvas))
    assertEquals(Interpreter.interpret(Session.initial, Command.DrawRect(1, 1, 3, 2)), Left(AppError.NoCanvas))
    assertEquals(Interpreter.interpret(Session.initial, Command.Fill(1, 1, 'o')), Left(AppError.NoCanvas))
    assertEquals(Interpreter.interpret(Session.initial, Command.Clear), Left(AppError.NoCanvas))

  test("render before canvas creation"):
    assertEquals(Interpreter.interpret(Session.initial, Command.Render), Left(AppError.NoCanvas))

  test("draw point"):
    val result = Interpreter.interpret(session, Command.DrawPoint(1, 1))
    assertEquals(result.map(_.session.canvas.map(_.charAt(1, 1))), Right(Some('*')))

  test("draw point out of bounds"):
    val result = Interpreter.interpret(session, Command.DrawPoint(20, 3))
    assertEquals(result, Left(AppError.OutOfBounds(20, 3)))

  test("draw horizontal line"):
    val rendered = renderAfter(List(Command.CreateCanvas(5, 3), Command.DrawLine(1, 1, 4, 1), Command.Render))
    assertEquals(rendered, Right(Some(".....\n.####\n.....")))

  test("draw vertical line"):
    val rendered = renderAfter(List(Command.CreateCanvas(5, 3), Command.DrawLine(2, 0, 2, 2), Command.Render))
    assertEquals(rendered, Right(Some("..#..\n..#..\n..#..")))

  test("draw diagonal line is rejected"):
    val result = Interpreter.interpret(session, Command.DrawLine(0, 0, 3, 3))
    assertEquals(result, Left(AppError.DiagonalLine))

  test("draw line out of bounds"):
    val result = Interpreter.interpret(session, Command.DrawLine(8, 1, 12, 1))
    assertEquals(result, Left(AppError.OutOfBounds(10, 1)))

  test("draw rect outline"):
    val rendered = renderAfter(List(Command.CreateCanvas(6, 4), Command.DrawRect(1, 1, 3, 2), Command.Render))
    assertEquals(rendered, Right(Some("......\n.###..\n.###..\n......")))

  test("draw rect with hollow center"):
    val rendered = renderAfter(List(Command.CreateCanvas(6, 5), Command.DrawRect(1, 1, 4, 3), Command.Render))
    assertEquals(rendered, Right(Some("......\n.####.\n.#..#.\n.####.\n......")))

  test("draw rect with invalid dimensions"):
    val result = Interpreter.interpret(session, Command.DrawRect(1, 1, 0, 2))
    assertEquals(result, Left(AppError.InvalidDimensions(0, 2)))

  test("draw rect out of bounds"):
    val result = Interpreter.interpret(session, Command.DrawRect(7, 1, 5, 2))
    assertEquals(result, Left(AppError.OutOfBounds(10, 1)))

  test("fill a bounded zone"):
    val rendered = renderAfter(
      List(Command.CreateCanvas(6, 5), Command.DrawRect(1, 1, 4, 3), Command.Fill(2, 2, 'o'), Command.Render)
    )
    assertEquals(rendered, Right(Some("......\n.####.\n.#oo#.\n.####.\n......")))

  test("fill the outside zone"):
    val rendered = renderAfter(
      List(Command.CreateCanvas(4, 2), Command.DrawPoint(0, 0), Command.Fill(3, 1, 'x'), Command.Render)
    )
    assertEquals(rendered, Right(Some("*xxx\nxxxx")))

  test("fill out of bounds"):
    val result = Interpreter.interpret(session, Command.Fill(20, 3, 'o'))
    assertEquals(result, Left(AppError.OutOfBounds(20, 3)))

  test("clear empties the canvas"):
    val rendered = renderAfter(
      List(Command.CreateCanvas(4, 2), Command.DrawPoint(1, 1), Command.Clear, Command.Render)
    )
    assertEquals(rendered, Right(Some("....\n....")))

  test("setchar changes the char of next drawings"):
    val rendered = renderAfter(
      List(Command.CreateCanvas(4, 2), Command.SetChar('x'), Command.DrawPoint(1, 1), Command.Render)
    )
    assertEquals(rendered, Right(Some("....\n.x..")))

  test("setchar is kept across canvas operations"):
    val result = interpretAll(List(Command.CreateCanvas(4, 2), Command.SetChar('x'), Command.Clear))
    assertEquals(result.map(_.session.drawChar), Right(Some('x')))

  test("advanced example from the assignment"):
    val rendered = renderAfter(
      List(
        Command.CreateCanvas(12, 6),
        Command.SetChar('x'),
        Command.DrawLine(11, 1, 11, 4),
        Command.SetChar('#'),
        Command.DrawRect(2, 1, 6, 4),
        Command.Fill(3, 2, 'o'),
        Command.Render
      )
    )
    val expected = List(
      "............",
      "..######...x",
      "..#oooo#...x",
      "..#oooo#...x",
      "..######...x",
      "............"
    ).mkString("\n")
    assertEquals(rendered, Right(Some(expected)))

  test("render outputs the grid"):
    val rendered = renderAfter(List(Command.CreateCanvas(5, 3), Command.Render))
    assertEquals(rendered, Right(Some(".....\n.....\n.....")))

  test("quit keeps state and produces no output"):
    assertEquals(Interpreter.interpret(session, Command.Quit), Right(Interpreter.Result(session, None)))
