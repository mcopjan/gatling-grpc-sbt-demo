import _root_.grpc.coingecko.coingecko.ExchangeServiceGrpc
import _root_.grpc.coingecko.coingecko.StoreExchangeRequest

import com.github.phisgr.gatling.grpc.Predef._
import io.gatling.core.Predef._
import io.grpc.health.v1.health.HealthCheckResponse.ServingStatus.SERVING
import io.grpc.health.v1.health.{HealthCheckRequest, HealthGrpc}
import com.github.phisgr.gatling.grpc.Predef._
import com.github.phisgr.gatling.pb._
// stringToExpression is hidden because we have $ in GrpcDsl
import io.gatling.core.Predef.{stringToExpression => _, _}
import io.gatling.core.session.Expression
import io.grpc.Status

import scala.concurrent.duration._
import _root_.grpc.coingecko.coingecko.Exchange

class BasicItSimulation extends Simulation {

  var grpcServerAddr = "localhost"
  var grpcServerPort = 6000
  val grpcConf = grpc(managedChannelBuilder(name = grpcServerAddr, port = grpcServerPort).usePlaintext())

  val scn = scenario("Example")
    .feed(csv("src/it/scala/coinGeckoExchange/resources/coinGeckoExchangeData.csv").circular)
    .exec(
      grpc("Store Exchange request")
       .rpc(ExchangeServiceGrpc.METHOD_STORE_EXCHANGE)
       .payload(StoreExchangeRequest.defaultInstance.updateExpr(
          _.exchange.name :~ $("name"),
          _.exchange.id :~ $("id"),
          _.exchange.image :~ $("image"),
          _.exchange.trustScoreRank :~ $("score_rank"),
          _.exchange.trustScore :~ $("score"),
          _.exchange.url :~ $("url"),
          _.exchange.yearEstablished :~ $("year_established"),
          _.exchange.hasTradingIncentive :~ $("has_trading_incentive"),
          _.exchange.tradeVolume24HBtcNormalized :~ $("trade_volume_24h_btc_normalized"),
          _.exchange.country :~ $("country")
          
        ))
    )


  setUp(scn.inject(
    rampUsersPerSec (1) to (50) during(1 minute))
    .protocols(grpcConf))
    //.assertions(global.responseTime.max lt (1000))
}
