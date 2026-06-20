/** 銘柄を表す。 */
export type StockSymbol = "Toyopa" | "Somy";

export const StockSymbol = {
  Toyopa: "Toyopa" as StockSymbol,
  Somy: "Somy" as StockSymbol,

  fromString(s: string): StockSymbol | undefined {
    if (s === "Toyopa" || s === "Somy") return s;
    return undefined;
  },
};
