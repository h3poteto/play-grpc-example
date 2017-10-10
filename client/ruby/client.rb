#!/usr/bin/env ruby

$LOAD_PATH.push('./lib')

require 'grpc'
require 'users_services_pb'
require 'securerandom'


class RequestClientInterceptor < GRPC::ClientInterceptor
  def request_response(request:, call:, method:, metadata: {})
    metadata['request_id'] = ::SecureRandom.uuid
    p "Intercepted request/response call at method #{method}" \
      " with request #{request} for call #{call}" \
      " and metadata: #{metadata}"
    yield
  end
end


def main
  stub = Users::Users::Stub.new("127.0.0.1:50051", :this_channel_is_insecure, interceptors: [RequestClientInterceptor.new])
  if ARGV.size == 1
    cw_user_id = stub.create(Users::CwUserId.new(id: ARGV[0].to_i))
    puts cw_user_id.inspect
  else
    stub.list(Users::RequestType.new).each do |x|
      puts x.inspect
    end
  end
end

main
