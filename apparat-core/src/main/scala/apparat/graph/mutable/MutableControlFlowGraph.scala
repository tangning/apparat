/*
 * This file is part of Apparat.
 *
 * Copyright (C) 2010 Joa Ebert
 * http://www.joa-ebert.com/
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package apparat.graph.mutable

import apparat.graph._
import annotation.tailrec

trait EntryVertex {
	override def toString() = "Entry"
}
trait ExitVertex {
	override def toString() = "Exit"
}

abstract class MutableControlFlowGraph[T, V <: BlockVertex[T]] extends MutableGraphWithAdjacencyMatrix[V] with ControlFlowGraphLike[V] with DOTExportAvailable[V] {
	override type G <: MutableControlFlowGraph[T, V]

	type ControlFlowVertex = V
	type ControlFlowEdge = E
	type ControlFlowElm = T
	type Block = Seq[T]

	protected[graph] def newEntryVertex: V

	protected[graph] def newExitVertex(): V

	override lazy val entryVertex = newEntryVertex

	override lazy val exitVertex = newExitVertex

	add(entryVertex)
	add(exitVertex)

	protected[graph] def add(block: Block)(implicit b2v: Block => V): V = {
		val blockAsV: V = block
		add(blockAsV)
		blockAsV
	}

	override def add(edge: E) = {
		assert(edge.kind != EdgeKind.Default)
		super.add(edge)
	}

	override def +(edge: E) = {
		add(edge)
		this
	}

	override def -(edge: E) = {
		remove(edge)
		this
	}

	def find(elm: T) = verticesIterator.find(_ contains elm)

	//error: name clash
	//def contains(elm: T) = verticesIterator.exists(_ contains elm)

	override def optimized = {simplified(); this}

	override def toString = "[MutableControlFlowGraph]"

	private def simplified() {
		val g = this
		var modified = false

		@tailrec def loop() {
			var vertices = g.verticesIterator.filterNot(p => p == entryVertex || p == exitVertex)

			//remove empty block
			vertices.filter(_.isEmpty).foreach {
				emptyVertex =>
					val out = g.outgoingOf(emptyVertex)
					if (out.size == 1 && out.head.kind == EdgeKind.Jump) {
						val endEdge = out.head
						g -= endEdge

						g.incomingOf(emptyVertex).foreach {
							startEdge => {
								g -= startEdge
								g += Edge.copy[V](startEdge, Some(startEdge.startVertex), Some(endEdge.endVertex))
							}
						}

						g -= emptyVertex

						modified = true
					}
			}

			//		// remove dead edge
			for (edge <- g.edgesIterator.filter(e => if (g.contains(e.startVertex)) {g.incomingOf(e.startVertex).isEmpty && !isEntry(e.startVertex)} else false)) {
				g -= edge
				g -= edge.startVertex

				modified = true
			}
			if (modified) {
				modified = false
				loop()
			}
		}
		loop()
	}

	def cleanString(str: String) = {
		val len = str.length
		@tailrec def loop(sb: StringBuilder, strIndex: Int): StringBuilder = {
			if (strIndex >= len)
				sb
			else {
				str(strIndex) match {
					case '"' => sb append "\\\""
					case '>' => sb append "&gt;"
					case '<' => sb append "&lt;"
					case '\r' => sb append "\\r"
					case '\n' => sb append "\\n"
					case '\t' => sb append "\\t"
					case c => sb append c
				}
				loop(sb, strIndex + 1)
			}
		}
		loop(new StringBuilder(), 0) toString
	}

	def label(value: String) = "label=\"" + cleanString(value) + "\""

	def vertexToString(vertex: V) = "[" + label({
		if (isEntry(vertex))
			"Entry"
		else if (isExit(vertex))
			"Exit"
		else
			vertex toString
	}) + "]"

	def edgeToString(edge: E) = "[" + label(edge match {
		case DefaultEdge(x, y) => ""
		case JumpEdge(x, y) => "  jump  "
		case TrueEdge(x, y) => "  true  "
		case FalseEdge(x, y) => "  false  "
		case DefaultCaseEdge(x, y) => "  default  "
		case CaseEdge(x, y) => "  case  "
		case NumberedCaseEdge(x, y, n) => "  case " + n
		case ThrowEdge(x, y) => "  throw  "
		case ReturnEdge(x, y) => "  return  "
	}) + "]"

	override def dotExport = {
		new DOTExport(this, (vertex: V) => vertexToString(vertex), (edge: E) => edgeToString(edge))
	}
}
