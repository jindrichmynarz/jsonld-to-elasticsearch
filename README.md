# jsonld-to-elasticsearch

A command-line tool that loads line-separated JSON-LD documents into Elasticsearch.

## Usage

Compile using [Leiningen](http://leiningen.org):

```sh
lein uberjar
```

Run from the command-line:

```sh
java -jar jsonld_to_elasticsearch.jar --help
```

You will need to provide the tool with several parameters. Provide configuration as an [EDN](https://github.com/edn-format/edn) file that contains the following keys:

* `endpoint`: A HTTP URL of the Elasticsearch REST API
* `index`: Name of the Elasticsearch index to use.
* `batch-size` (optional, default = 5000): Number of documents to index in one batch.

Besides the configuration, you will need to provide an Elasticsearch [mapping](https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping.html) for the documents you want to index. The mapping must contain exactly one mapping type, which will be used as the type of the indexed documents.

The input JSON-LD documents can be read from a file or by default from the standard input. The tool expects the JSON-LD provided as [Newline Delimited JSON](http://ndjson.org), with each document on a single line. `@context` and `@id` attributes are removed from each document, while `@id` is used as the Elasticsearch [`_id` field](https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping-id-field.html). You can prepare JSON-LD documents in the expected format using [sparql-to-jsonld](https://github.com/jindrichmynarz/sparql-to-jsonld).

## License

Copyright © 2016 Jindřich Mynarz

Distributed under the Eclipse Public License version 1.0. 
