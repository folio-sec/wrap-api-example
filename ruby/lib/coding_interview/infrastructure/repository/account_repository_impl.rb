require_relative "../../application/repository/account_repository"

module CodingInterview
  module Infrastructure
    module Repository
      class AccountRepositoryImpl < Application::Repository::AccountRepository
        def initialize
          @store = {}
          @mutex = Mutex.new
        end

        def find(user_id)
          @mutex.synchronize { @store[user_id.value] }
        end

        def upsert(user_id, account)
          @mutex.synchronize { @store[user_id.value] = account }
          nil
        end

        def exists?(user_id)
          @mutex.synchronize { @store.key?(user_id.value) }
        end
      end
    end
  end
end
