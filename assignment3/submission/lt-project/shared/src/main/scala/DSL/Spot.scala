package DSL


sealed trait Spot {
  type A <: Spot

  val position: Point = Point(0, 0)

  def change(property: CanvasElementModifier[A]): Unit = property.change(this.asInstanceOf[A])

  def move(p: Point): Unit = {
    position.x = p.x
    position.y = p.y
  }
}


trait SpotAttributes {
  var color = "black"
  var strokeWidth = 1

  def color(col: String): Unit = color = col

  def strokeWidth(n: Int): Unit = strokeWidth = n
}


// Spot representing a wall.
case class Wall(override val position: Point, size: Int) extends Spot with SpotAttributes {
  type A = Wall
}


// Spot representing an apple.
case class Apple(override val position: Point, size: Int) extends Spot with SpotAttributes {
  type A = Apple
}

// Spot representing an empty square.
case class Empty(override val position: Point, size: Int) extends Spot with SpotAttributes {
  type A = Empty
}

// Spot representing a snake block.
case class Snake(override val position: Point, size: Int) extends Spot with SpotAttributes {
  type A = Snake
}

// Spot representing a ball.
case class Ball(override val position: Point, size: Int) extends Spot with SpotAttributes {
  type A = Ball
}

// Spot representing a paddle block.
case class Player(override val position: Point, size: Int) extends Spot with SpotAttributes {
  type A = Player
}

// Spot representing a score.
case class Score(override val position: Point, size: Int, var score: Int) extends Spot with SpotAttributes {
  type A = Score

  var font = "20px Helvetica"
}

// Spot representing a message.
case class Message(override val position: Point, size: Int, text: String) extends Spot with SpotAttributes {
  type A = Message

  var font = "20px Helvetica"
}

// ComposedSpot (with a list of Spots).
case class ComposedSpot[T <: Spot](var l: Seq[T]) extends Spot {
  type A = T

  def map(f: Spot => Spot): ComposedSpot[Spot] = ComposedSpot(l.map(f))

  def flatMap(f: Spot => Iterable[Spot]): ComposedSpot[Spot] = ComposedSpot(l.flatMap(f))

  def foreach[B](f: Spot => B): Unit = {
    if (l.nonEmpty) {
      f(l.head)
      l.tail.foreach(f)
    }
  }

  def apply(i: Int): Spot = {
    if (i < 0) {
      throw new IndexOutOfBoundsException
    } else if (i == 0) {
      l.head
    } else {
      l.tail(i - 1)
    }
  }

  def prepend(sb: A): Unit = l = sb +: l

  def contains(p: Point): Boolean = l.map(_.position).contains(p)

  def head: Spot = l.head

  def last: Spot = l.last

  def size: Int = l.length

  def remove(p: Point): Unit = l = l.filterNot(_.position == p)

  override def change(property: CanvasElementModifier[A]): Unit = {
    l.foreach(x => x.change(property.asInstanceOf[CanvasElementModifier[x.A]]))
  }

  override def move(p: Point): Unit = {
    var oldPosition: Point = p
    for (s <- l) {
      val newPosition: Point = Point(s.position.x, s.position.y)
      s.move(oldPosition)
      oldPosition = newPosition
    }
  }

  def translate(direction: Point): Unit = {
    l.foreach(x => x.move(x.position + direction))
  }
}

// ComposedSpot2D with a 2D matrix of spots.
case class ComposedSpot2D[T <: Spot](spots: Array[Array[T]]) extends Spot {
  type A = T

  case class ArrayMapper(i: Int) {
    def apply(j: Int): Spot = spots(i)(j)
    def update(j: Int, v: T): Unit = spots(i)(j) = v
    def indices: Range = spots(i).indices
  }

  def apply(i: Int): ArrayMapper = ArrayMapper(i)
  def indices: Range = spots.indices

  override def change(property: CanvasElementModifier[A]): Unit = {
    spots.foreach(_.foreach(x => x.change(property.asInstanceOf[CanvasElementModifier[x.A]])))
  }
}

// Makes the old main file work.
object Extends {
  implicit class shapeArrayExtend[T <: Spot](s: Array[T]) {
    type A = T

    def change(property: CanvasElementModifier[A]): Unit = {
      s.foreach(x => x.change(property.asInstanceOf[CanvasElementModifier[x.A]]))
    }
  }
}