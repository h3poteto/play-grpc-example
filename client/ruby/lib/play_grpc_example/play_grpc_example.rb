require 'grpc'
require 'protocol/users_services_pb'
require 'securerandom'

module PlayGrpcExample
  module Client
    class User
      attr_accessor :stub
      def initialize
        @stub = Protocol::User::Users::Stub.new("127.0.0.1:50051", :this_channel_is_insecure, interceptors: [RequestClientInterceptor.new])
      end

      def create(id:, name:, age:)
        @stub.create(Protocol::User::User.new(id: id, name: name, age: age))
      end

      def list
        @stub.list(Protocol::User::RequestType.new).each do |u|
          puts u.inspect
        end
      end
    end
  end
end

# リクエストにオリジナルのIDを仕込むためのinterceptorを挟む
class RequestClientInterceptor < GRPC::ClientInterceptor
  def request_response(request:, call:, method:, metadata: {})
    metadata['request_id'] = ::SecureRandom.uuid
    p "Intercepted request/response call at method #{method}" \
      " with request #{request} for call #{call}" \
      " and metadata: #{metadata}"
    yield
  end

  def server_streamer(request:, call:, method:, metadata: {})
    metadata['request_id'] = ::SecureRandom.uuid
    p "Intercepted request/response call at method #{method}" \
      " with request #{request} for call #{call}" \
      " and metadata: #{metadata}"
    yield
  end
end
