package edu.knowitall.tool.document

import edu.knowitall.repr.document.Document
import edu.knowitall.repr.sentence.Sentence
import edu.knowitall.repr.sentence.Extracted
import edu.knowitall.repr.document.Sentenced
import edu.knowitall.repr.document.DocumentSentence
import edu.knowitall.repr.extraction.Extraction
import edu.knowitall.repr.sentence.Parsed
import edu.knowitall.repr.sentence.Chunked
import edu.knowitall.repr.coref.CorefResolved
import edu.knowitall.tool.sentence.OpenIEExtracted
import edu.knowitall.repr.sentence.Lemmatized
import edu.knowitall.tool.parse.ClearParser
import edu.knowitall.tool.chunk.OpenNlpChunker
import edu.knowitall.tool.coref.StanfordCorefResolver
import edu.knowitall.tool.coref.Mention
import edu.knowitall.tool.stem.MorphaStemmer
import edu.knowitall.tool.link.OpenIELinked
import edu.knowitall.browser.entity.EntityLinker
import java.io.File
import edu.knowitall.tool.bestentitymention.BestEntityMentionsFound
import edu.knowitall.tool.bestentitymention.BestEntityMentionFinderOriginalAlgorithm
import edu.knowitall.repr.link.LinkedDocument
import edu.knowitall.repr.link.FreeBaseLink
import edu.knowitall.repr.bestentitymention.BestEntityMentionResolvedDocument
import edu.knowitall.repr.bestentitymention.BestEntityMention
import edu.knowitall.repr.coref.MentionCluster

class OpenIEDocumentExtractor {

  type InputDoc = Document with Sentenced[_ <: Sentence]
  type OutputDoc = Document with OpenIELinked with CorefResolved[Mention] with Sentenced[Sentence with OpenIEExtracted] with BestEntityMentionsFound
  
  val parser = new ClearParser()
  val chunker = new OpenNlpChunker()
  val stemmer = new MorphaStemmer()
  val entityLinker = new EntityLinker(new File("/scratch/resources/entitylinkingResources/"))
  val stanfordResolver = new StanfordCorefResolver()
  val bestEntityMentionFinderAlgorithm = new BestEntityMentionFinderOriginalAlgorithm()


  def prepSentence(s: Sentence): Sentence with OpenIEExtracted = {
    val parse = parser(s.text)
    val postokens = parse.nodes.toSeq
    val chunkTokens = chunker.chunkPostagged(postokens)
    new Sentence(s.text) with OpenIEExtracted with Parsed with Chunked with Lemmatized {
      override val lemmatizedTokens = chunkTokens map stemmer.stemToken
      override val dgraph = parse
      override val tokens = chunkTokens
    }
  }

  def extract(d: Document with Sentenced[_ <: Sentence]): Document with OpenIELinked with CorefResolved[Mention] with Sentenced[Sentence with OpenIEExtracted] with BestEntityMentionsFound = {

    val preppedSentences = d.sentences.map { case DocumentSentence(sentence, offset) =>
      DocumentSentence(prepSentence(sentence), offset)
    }

    new Document(d.text) with OpenIELinked with CorefResolved[Mention] with Sentenced[Sentence with OpenIEExtracted] with BestEntityMentionsFound {
      val clusters = stanfordResolver.resolve(d)
      val sentences = preppedSentences
      val linker = entityLinker
      val bestEntityMentionFinder = bestEntityMentionFinderAlgorithm
    }
  }
}

class OpenIENoCorefDocumentExtractor {

  type InputDoc = Document with Sentenced[_ <: Sentence]
  type OutputDoc = Document with OpenIELinked with Sentenced[Sentence with OpenIEExtracted] with BestEntityMentionsFound
  
  val parser = new ClearParser()
  val chunker = new OpenNlpChunker()
  val stemmer = new MorphaStemmer()
  val entityLinker = new EntityLinker(new File("/scratch/"))
  val bestEntityMentionFinderAlgorithm = new BestEntityMentionFinderOriginalAlgorithm()


  def prepSentence(s: Sentence): Sentence with OpenIEExtracted = {
    val parse = parser(s.text)
    val postokens = parse.nodes.toSeq
    val chunkTokens = chunker.chunkPostagged(postokens)
    new Sentence(s.text) with OpenIEExtracted with Parsed with Chunked with Lemmatized {
      override val lemmatizedTokens = chunkTokens map stemmer.stemToken
      override val dgraph = parse
      override val tokens = chunkTokens
    }
  }

  def extract(d: Document with Sentenced[_ <: Sentence]): Document with OpenIELinked with Sentenced[Sentence with OpenIEExtracted] with BestEntityMentionsFound = {

    val preppedSentences = d.sentences.map { case DocumentSentence(sentence, offset) =>
      DocumentSentence(prepSentence(sentence), offset)
    }

    new Document(d.text) with OpenIELinked with Sentenced[Sentence with OpenIEExtracted] with BestEntityMentionsFound {
      val sentences = preppedSentences
      val linker = entityLinker
      val bestEntityMentionFinder = bestEntityMentionFinderAlgorithm
    }
  }
}

class OpenIEBaselineExtractor {

  type InputDoc = Document with Sentenced[_ <: Sentence]
  type OutputDoc = Document with OpenIELinked with Sentenced[Sentence with OpenIEExtracted]  
  
  val parser = new ClearParser()
  val chunker = new OpenNlpChunker()
  val stemmer = new MorphaStemmer()
  val entityLinker = new EntityLinker(new File("/scratch/"))

  def prepSentence(s: Sentence): Sentence with OpenIEExtracted = {
    val parse = parser(s.text)
    val postokens = parse.nodes.toSeq
    val chunkTokens = chunker.chunkPostagged(postokens)
    new Sentence(s.text) with OpenIEExtracted with Parsed with Chunked with Lemmatized {
      override val lemmatizedTokens = chunkTokens map stemmer.stemToken
      override val dgraph = parse
      override val tokens = chunkTokens
    }
  }

  def extract(d: Document with Sentenced[_ <: Sentence]): Document with OpenIELinked with Sentenced[Sentence with OpenIEExtracted] = {

    val preppedSentences = d.sentences.map { case DocumentSentence(sentence, offset) =>
      DocumentSentence(prepSentence(sentence), offset)
    }

    new Document(d.text) with OpenIELinked with Sentenced[Sentence with OpenIEExtracted] {
      val sentences = preppedSentences
      val linker = entityLinker
    }
  }
}

class OpenIECorefExpandedDocumentExtractor {
  val parser = new ClearParser()
  val chunker = new OpenNlpChunker()
  val stemmer = new MorphaStemmer()
  val entityLinker = new EntityLinker(new File("/scratch/resources/entitylinkingResources/"))
  val stanfordResolver = new StanfordCorefResolver()
  val bestEntityMentionFinderAlgorithm = new BestEntityMentionFinderOriginalAlgorithm()


  def prepSentence(s: Sentence): Sentence with OpenIEExtracted = {
    val parse = parser(s.text)
    val postokens = parse.nodes.toSeq
    val chunkTokens = chunker.chunkPostagged(postokens)
    new Sentence(s.text) with OpenIEExtracted with Parsed with Chunked with Lemmatized {
      override val lemmatizedTokens = chunkTokens map stemmer.stemToken
      override val dgraph = parse
      override val tokens = chunkTokens
    }
  }
  
  def getUniqueLinksInCluster(cluster: MentionCluster[Mention], links : Seq[FreeBaseLink]): Seq[FreeBaseLink] = {
    var linksInCluster = Seq[FreeBaseLink]()
    for(m <- cluster.mentions){
      for(link <- links){
        if(link.offset == m.offset){
          linksInCluster = linksInCluster :+ link
        }
      }
    }
    linksInCluster.groupBy(f => f.id).values.map(f => f.head).toSeq
  }
  
  def getUniqueBestEntityMentionsInCluster(cluster: MentionCluster[Mention], bestEntityMentions: Seq[BestEntityMention]): Seq[BestEntityMention] = {
    var bestEntityMentionsInCluster = Seq[BestEntityMention]()
    for(m <- cluster.mentions){
      for(bem <- bestEntityMentions){
        if(bem.offset == m.offset){
          bestEntityMentionsInCluster = bestEntityMentionsInCluster :+ bem
        }
      }
    }
    bestEntityMentionsInCluster.groupBy(f => f.bestEntityMention).values.map(f => f.head).toSeq
  }
  
  def makeNewBestEntityMentionsForPronounsInCluster(cluster: MentionCluster[Mention], bestName: String) = {
    for(m <- cluster.mentions; if m.isPronoun) yield BestEntityMention(m.text,m.offset,bestName)
  }
  
  def extract(d: Document with Sentenced[_ <: Sentence], debug: Boolean): Document with LinkedDocument[FreeBaseLink] with CorefResolved[Mention] with Sentenced[Sentence with OpenIEExtracted] with BestEntityMentionResolvedDocument[BestEntityMention] = {

    val preppedSentences = d.sentences.map { case DocumentSentence(sentence, offset) =>
      DocumentSentence(prepSentence(sentence), offset)
    }

    val doc  = new Document(d.text) with OpenIELinked with CorefResolved[Mention] with Sentenced[Sentence with OpenIEExtracted] with BestEntityMentionsFound {
      val clusters = stanfordResolver.resolve(d)
      val sentences = preppedSentences
      val linker = entityLinker
      val bestEntityMentionFinder = bestEntityMentionFinderAlgorithm
    }
    if(debug){
	    println("Document : " + d.sentences.head)
	    println("All links in Doc: ")
	    for(link <- doc.links){
	      println(link.offset + "\t" + link.name)
	    }
	    println("All BEMS in Doc: ")
	    for(bem <- doc.bestEntityMentions){
	      println(bem.offset + "\t" + bem.bestEntityMention)
	    }
    }
    var corefExpandedBestEntityMentions = doc.bestEntityMentions
    var clusterIndex =1
    for(cluster <- doc.clusters){
      val mentions = cluster.mentions
      val links = getUniqueLinksInCluster(cluster,doc.links)
      val bestEntityMentions = getUniqueBestEntityMentionsInCluster(cluster, doc.bestEntityMentions)
      var bestName :Option[String] = None
      if(debug){
	      println("Cluster number " + clusterIndex)
	      println("Mentions: ")
	      for(mention <- mentions){
	        println(mention.offset + "\t" + mention.text + "\t" + mention.isPronoun)
	      }
	      println("Unique links: ")
	      for(l <- links){
	        println(l.offset + "\t" + l.name)
	      }
	      println("Unique BEMS: ")
	      for(bem <- bestEntityMentions){
	        println(bem.offset + "\t" + bem.bestEntityMention)
	      }
      }
      if(links.length ==1)  {
        bestName = Some(links.head.name)
      }
      else if(bestEntityMentions.length ==1) {
        bestName = Some(bestEntityMentions.head.bestEntityMention)
      }
      if(bestName.isDefined){
        val newBestEntityMentions = makeNewBestEntityMentionsForPronounsInCluster(cluster,bestName.get)
        corefExpandedBestEntityMentions = corefExpandedBestEntityMentions ++ newBestEntityMentions
      }
      clusterIndex += 1
    }
    if(debug){
	    println("New BEMS:")
	    for(newMention <- corefExpandedBestEntityMentions){
	      if(doc.bestEntityMentions.forall(p => p.offset != newMention.offset)){
	        println(newMention.offset +"\t" + newMention.bestEntityMention)
	      }
	    }
    }
    val newDoc = new Document(d.text) with LinkedDocument[FreeBaseLink] with CorefResolved[Mention] with Sentenced[Sentence with OpenIEExtracted] with BestEntityMentionResolvedDocument[BestEntityMention]{
      val links = doc.links
      val clusters = doc.clusters
      val sentences = doc.sentences
      val bestEntityMentions = corefExpandedBestEntityMentions.toSeq
    }
    newDoc
  }
}
