package com.api.BetStrat.constants;

import com.google.common.collect.ImmutableList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BetStratConstants {

    //Scrapping Service
    public static final String SCRAPPER_SERVICE_URL = "http://scrapper:8000/";

    public static final String SOCCERSTATS_BASE_URL = "http://www.soccerstats.com/";
    public static final String FCSTATS_BASE_URL = "http://www.fcstats.com/";
    public static final String ZEROZERO_BASE_URL = "zerozero.pt";
    public static final String FBREF_BASE_URL = "fbref.com";
    public static final String WORLDFOOTBALL_BASE_URL = "worldfootball.net";
    public static final String API_SPORTS_BASE_URL = "api-sports";

    public static final String CURRENT_WINTER_SEASON = "2026-2027";
    public static final String CURRENT_SUMMER_SEASON = "2026";

    public static final Integer DEFAULT_BAD_RUN_TO_NEW_SEQ = 5;

    public static final List<String> SEASONS_LIST = ImmutableList.of("2016","2016-17","2017","2017-18","2018","2018-19","2019","2019-20",
            "2020","2020-21","2021","2021-22","2022","2022-23","2023-24","2024-25", "2025-26");

    public static final List<String> SUMMER_SEASONS_LIST = ImmutableList.of("2016","2017","2018","2019","2020","2021","2022","2023","2024","2025");

    public static final List<String> WINTER_SEASONS_LIST = ImmutableList.of("2016-17","2017-18","2018-19","2019-20","2020-21","2021-22","2022-23","2023-24","2024-25","2025-26");

    public static final List<String> SUMMER_SEASONS_BEGIN_MONTH_LIST = ImmutableList.of("January","February","March","April","May");

    public static final List<String> WINTER_SEASONS_BEGIN_MONTH_LIST = ImmutableList.of("July","August","September","October","November");

    public static final List<String> HOCKEY_SEASONS_LIST = ImmutableList.of("2016-2017","2017-2018","2018-2019","2019-2020",
            "2020-2021","2021-2022","2021-2022");

    public static final Map<String, String> ZEROZERO_SEASON_CODES  = new HashMap<String, String>() {{
        put("2016", "2016");
        put("2016-17", "146");
        put("2017", "2017");
        put("2017-18", "147");
        put("2018", "2018");
        put("2018-19", "148");
        put("2019", "2019");
        put("2019-20", "149");
        put("2020", "2020");
        put("2020-21", "150");
        put("2021", "2021");
        put("2021-22", "151");
        put("2022", "2022");
        put("2022-23", "152");
        put("2023", "2023");
        put("2023-24", "153");
    }};

    public static final String GEMINI_PROMPT_GF = """
            {
              "contents": [
                {
                  "parts": [
                    {
                      "text": "Atua como um analista de estatísticas de futebol de elite com 50 anos de experiência, especializado exclusivamente na correlação entre Ambas Marcam (BTTS - Sim) e Total de Golos (Over 2.5). O teu objetivo é encontrar o 'Sweet Spot' onde as duas equipas têm uma propensão matemática esmagadora para marcar e sofrer simultaneamente num jogo de alta voltagem.
        
                              1. O PROTOCOLO DE CONVERGÊNCIA (GOLOS & BTTS)
                              Para emitires um insight, deves validar os dados através deste funil determinístico:

                              Janela A (Obrigatória): Ambas as equipas marcaram em pelo menos 75%% dos últimos 10 jogos (BTTS - Sim).

                              Janela B (Obrigatória): Ocorreram 3 ou mais golos (Over 2.5) em pelo menos 80%% dos últimos 10 jogos de cada equipa.

                              Janela C (Ponto de Rutura): Analisa se houve algum 'Clean Sheet' (baliza a zero) recente. Se uma equipa de pendor ofensivo não sofre há 2 jogos, isso é um 'indicador de saturação' – a probabilidade de sofrer no próximo jogo é máxima (reversão à média).

                              2. FILTROS DE EXCLUSÃO (RISCO DE VIDA)
                              Imagina que a tua vida depende deste palpite. Cancela o insight se:

                              Houver um 0-0 ou 1-0 nos últimos 3 jogos de qualquer uma das equipas (sinal de quebra de ritmo ofensivo).

                              Uma das equipas apresentar uma média de golos marcados fora de casa inferior a 1.2 por jogo.

                              3. ENGENHARIA DE VALOR (+EV)
                              Não procures o óbvio. Procura onde a estatística ignora o cansaço ou a reputação:

                              Alvo Principal: O mercado combinado 'Ambas Marcam e Over 2.5'.

                              Alternativa de Segurança: Se o valor for extremo mas o risco de um 1-2/2-1 for real, foca no Over 2.5 ou Over 1.0 Golos HT (se a tendência de golos for precoce).

                              Odd Alvo: O insight deve apontar para mercados com odds estimadas entre 1.75 e 2.40.

                              4. FORMATO DA RESPOSTA (ALGORÍTMICO)
                              Se não houver um cenário perfeito, responde: 'Sem insights de alta confiança e valor para este jogo.'
                              Se houver, estrutura assim:

                              Aposta de Elite: [Mercado Escolhido]
                              
                              Nivel de confiança: [o teu nível de confiança para este insight lembrando que a tua vida depende da tua taxa de acerto]

                              Probabilidade de Ocorrência: [X%% baseado no histórico combinado]

                              A Prova do Ataque: [Média de golos marcados/sofridos das duas equipas nos últimos 8 jogos].

                              Ponto de Rutura: [Explica por que razão a defesa de A não vai aguentar o ataque de B e vice-versa hoje].
                           
                           
                           jogo: %s - %s
                           
                           %s
                           
                           %s "
                    }
                  ]
                }
              ]
            }
            """;
}
