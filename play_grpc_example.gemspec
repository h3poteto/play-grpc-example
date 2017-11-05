# coding: utf-8
lib = File.expand_path('../client/ruby/lib', __FILE__)
$LOAD_PATH.unshift(lib) unless $LOAD_PATH.include?(lib)
require 'play_grpc_example/version'

Gem::Specification.new do |spec|
  spec.name          = "play_grpc_example"
  spec.version       = PlayGrpcExample::VERSION
  spec.authors       = ["h3poteto"]
  spec.email         = ["h3.poteto@gmail.com"]

  spec.summary       = %q{A gem for play-grpc-example.}
  spec.description   = %q{A gem for play-grpc-example.}
  spec.homepage      = "https://github.com/h3poteto/play-grpc-example"
  spec.license       = "MIT"

  spec.files         = Dir['client/ruby/lib/*.rb']
#  spec.files         = `git ls-files -z`.split("\x0").reject { |f| f.match(%r{^(test|spec|features)/}) }
  spec.bindir        = "exe"
  spec.executables   = spec.files.grep(%r{^exe/}) { |f| File.basename(f) }
  spec.test_files    = Dir.glob('client/ruby/spec/**/*')
  spec.require_paths = ["client/ruby/lib", "client/ruby/lib/play_grpc_example" ,"client/ruby/spec"]

  spec.add_runtime_dependency "grpc", "~> 1.7"

  spec.add_development_dependency "bundler", "~> 1.15"
  spec.add_development_dependency "rake", "~> 12.1"
  spec.add_development_dependency "grpc-tools", "~> 1.6"
end
