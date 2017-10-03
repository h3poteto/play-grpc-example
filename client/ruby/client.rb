#!/usr/bin/env ruby

$LOAD_PATH.push('./lib')

require 'grpc'
require 'users_services_pb'

def main
  stub = Users::Users::Stub.new("127.0.0.1:50051", :this_channel_is_insecure)
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
