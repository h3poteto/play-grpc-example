#!/usr/bin/env ruby

$LOAD_PATH.push('./lib')

require 'grpc'
require 'users_services_pb'

def main
  stub = Users::Users::Stub.new("127.0.0.1:50051", :this_channel_is_insecure)
  cw_user_id = stub.create(Users::CwUserId.new(id: 1))
  puts cw_user_id.inspect
end

main
