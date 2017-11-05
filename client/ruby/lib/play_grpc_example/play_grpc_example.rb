require 'grpc'
require 'protocol/users_services_pb'
require 'securerandom'

module PlayGrpcExample
  module Client
    class User
      attr_accessor :stub
      def initialize
        @stub = Proto::Users::Stub.new("127.0.0.1:50051", :this_channel_is_insecure, interceptors: [RequestClientInterceptor.new])
      end

      def create(id:, name:, age:)
        @stub.create(Proto::User.new(id: id, name: name, age: age))
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
