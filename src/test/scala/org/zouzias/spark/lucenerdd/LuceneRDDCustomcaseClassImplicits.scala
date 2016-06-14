/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zouzias.spark.lucenerdd

import com.holdenkarau.spark.testing.SharedSparkContext
import org.apache.lucene.document.Document
import org.scalatest.{BeforeAndAfterEach, FlatSpec, Matchers}

case class Person(name: String, age: Int, email: String)

object Person extends Serializable {
  implicit def personToDocument(person: Person): Document = {
    val doc = new Document
    typeToDocument(doc, "name", person.name)
    typeToDocument(doc, "age", person.age)
    typeToDocument(doc, "email", person.email)
    doc
  }
}


class LuceneRDDCustomcaseClassImplicits extends FlatSpec
  with Matchers
  with BeforeAndAfterEach
  with SharedSparkContext {

  var luceneRDD: LuceneRDD[_] = _

  override def afterEach() {
    luceneRDD.close()
  }

  import Person._

  def randomString(length: Int): String = scala.util.Random.alphanumeric.take(length).mkString

  val people = (1 to 24).map(randomString(_))
    .zipWithIndex.map{ case (str, index) => Person(str, index, s"${str}@gmail.com")}

  "LuceneRDD(case class).count" should "return correct number of elements" in {
    val rdd = sc.parallelize(people)
    luceneRDD = LuceneRDD(rdd)
    luceneRDD.count should equal (people.size)
  }

  "LuceneRDD(case class).fields" should "return all fields" in {
    val rdd = sc.parallelize(people)
    luceneRDD = LuceneRDD(rdd)

    luceneRDD.fields().size should equal(3)
    luceneRDD.fields().contains("name") should equal(true)
    luceneRDD.fields().contains("age") should equal(true)
    luceneRDD.fields().contains("email") should equal(true)
  }

  "LuceneRDD(case class).termQuery" should "correctly search with TermQueries" in {
    val rdd = sc.parallelize(people)
    luceneRDD = LuceneRDD(rdd)
    val results = luceneRDD.termQuery("name",
      people(scala.util.Random.nextInt(people.size)).name)
    results.size should equal (1)
  }

}
