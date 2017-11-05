# Play grpc example

## Update gem library

Generate library files from proto files using following command:

```bash
$ bundle exec grpc_tools_ruby_protoc -I ./protocol --ruby_out=client/ruby/lib/play_grpc_example --grpc_out=client/ruby/lib/play_grpc_example ./protocol/**/*.proto
```


