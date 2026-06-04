module CodingInterview
  module Application
    module Repository
      # 口座管理リポジトリ。
      class AccountRepository
        def find(_user_id); raise NotImplementedError; end
        def upsert(_user_id, _account); raise NotImplementedError; end
        def exists?(_user_id); raise NotImplementedError; end
      end
    end
  end
end
