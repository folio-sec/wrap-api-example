module CodingInterview
  module Domain
    # 銘柄を表す。
    module StockSymbol
      Toyopa = :Toyopa
      Somy = :Somy

      ALL = [Toyopa, Somy].freeze

      def self.from_string(s)
        case s
        when "Toyopa" then Toyopa
        when "Somy" then Somy
        end
      end
    end
  end
end
