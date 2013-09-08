async-crawler
=============

[![Build Status](https://jenkins.nisshiee.org/buildStatus/icon?job=async-crawler)](https://jenkins.nisshiee.org/job/async-crawler/)

async-crawlerは一発稼働系クローラーを簡単に作成するサポートをするライブラリです。

主なコンセプトは以下の通りです。

  - 一定のSleepタイムを強制的に挟むことでDos攻撃にならないよう調整を自動で入れる
  - Futureを用いた非同期リクエスト
  - レスポンスの柔軟な解析と解析部分の高いモジュラリティ
  - ScalazのMonad型クラスと連携してfor, map, flatMap等を用いたコーディングを実現

How to use
----------

### sbt

```
libraryDependencies += "org.nisshiee" %% "async-crawler" % "1.0.0"

// 各種解析補助モジュールを利用する場合は以下のモジュールを利用できます

libraryDependencies += "org.nisshiee" %% "async-crawler-jsoup" % "1.0.0"

libraryDependencies += "org.nisshiee" %% "async-crawler-json4s" % "1.0.0"

libraryDependencies += "org.nisshiee" %% "async-crawler-filedl" % "1.0.0"
```

### coding example

Jsoupを使ってTextNodeを抽出する例

```scala
import org.nisshiee.crawler._
import org.nisshiee.crawler.jsoup._
import org.jsoup.nodes.Document
import scalaz._, Scalaz._

val f: Fv[String] = Crawler("http://example.com") { doc: Document =>
  doc.select("h1").first.text successNel
}

f.result() // => Success("Example Domain")

Crawler.shutdown
```

深さ2までリンク辿ってURLリストを抽出する例

```scala
import scala.collection.JavaConverters._
import org.nisshiee.crawler._
import org.nisshiee.crawler.jsoup._
import org.jsoup.nodes.Document
import scalaz._, Scalaz._

def links(url: String): Fv[List[String]] = Crawler(url) { doc: Document =>
  doc.select("a[href]").asScala.toList map { _.attr("href") } successNel
}

val f: Fv[List[String]] = (for {
  url1 <- ListT(links("http://example.com"))
  url2 <- ListT(links(url1))
} yield url2) underlying

f.result() // => Success(List(...))

Crawler.shutdown
```

License
------------

MIT


