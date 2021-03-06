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
package apparat.graph

/**
 * @author Joa Ebert
 */
trait DefaultDOTExport[V] extends DOTExportAvailable[V] {
	self: GraphLike[V] =>

	override def dotExport = {
		def label(value: String) = "[label=\"" + value + "\"]"
		new DOTExport(this, (vertex: V) => label(vertex.toString), (edge: E) => edge match {
			case DefaultEdge(x, y) => ""
			case JumpEdge(x, y) => label("  jump  ")
			case TrueEdge(x, y) => label("  true  ")
			case FalseEdge(x, y) => label("  false  ")
			case DefaultCaseEdge(x, y) => label("  default  ")
			case CaseEdge(x, y) => label("  case  ")
			case NumberedCaseEdge(x, y, n) => label("  case " + n)
			case ThrowEdge(x, y) => label("  throw  ")
			case ReturnEdge(x, y) => label("  return  ")
		})
	}
}
