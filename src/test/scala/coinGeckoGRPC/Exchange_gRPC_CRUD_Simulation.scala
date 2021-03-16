import _root_.grpc.coingecko.coingecko.{ExchangeServiceGrpc,StoreExchangeRequest, StoreExchangeResponse, GetExchangeRequest, GetExchangeResponse,UpdateExchangeRequest, UpdateExchangeResponse, Exchange}

import io.gatling.core.Predef._
import com.github.phisgr.gatling.grpc.Predef._
import com.github.phisgr.gatling.pb._
// stringToExpression is hidden because we have $ in GrpcDsl
import io.gatling.core.Predef.{stringToExpression => _, _}
import io.gatling.core.session.Expression
import io.grpc.Status
import scala.concurrent.duration._
import _root_.grpc.coingecko.coingecko.DeleteExchangeRequest

class Exchange_gRPC_CRUD_Simulation extends Simulation {

  var grpcServerAddr = "localhost"
  var grpcServerPort = 6000
  val grpcConf = grpc(managedChannelBuilder(name = grpcServerAddr, port = grpcServerPort).usePlaintext())

  val scn = scenario("CRUD Exchanges")
    .feed(csv("coinGeckoExchangeData.csv").circular)
    .exec(
      grpc("Store exchange")
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
       .extract(_.result.get.mongoId.some)(_ saveAs "id")
       .check(statusCode is Status.Code.OK)
    )
    .exec(
      grpc("Get Exchange")
      .rpc(ExchangeServiceGrpc.METHOD_GET_EXCHANGE)
      .payload(GetExchangeRequest.defaultInstance.updateExpr(
        _.id :~ $("id")
      ))
      .check(statusCode is Status.Code.OK)
    )
    .exec(
      grpc("Update Exchange")
      .rpc(ExchangeServiceGrpc.METHOD_UPDATE_EXCHANGE)
      .payload(UpdateExchangeRequest.defaultInstance.updateExpr(
          _.exchange.mongoId :~ $("id"),
          _.exchange.id :~ $("id"),
          _.exchange.image :~ $("image"),
          _.exchange.trustScoreRank :~ $("score_rank"),
          _.exchange.trustScore :~ $("score"),
          _.exchange.url :~ $("url"),
          _.exchange.yearEstablished :~ "2020",
          _.exchange.hasTradingIncentive :~ $("has_trading_incentive"),
          _.exchange.tradeVolume24HBtcNormalized :~ $("trade_volume_24h_btc_normalized"),
          _.exchange.country :~ $("country"),
          _.exchange.name :~ $("name")
      ))
      .check(statusCode is Status.Code.OK)
    )
    .exec(
      grpc("Delete Exchange")
      .rpc(ExchangeServiceGrpc.METHOD_DELETE_EXCHANGE)
      .payload(DeleteExchangeRequest.defaultInstance.updateExpr(
        _.id :~ $("id")
      ))
      .check(statusCode is Status.Code.OK)
    );
    
    
    

  setUp(scn.inject(
    nothingFor(4.seconds),
    constantUsersPerSec (10) during(10 seconds))
    .protocols(grpcConf))
    //.assertions(global.responseTime.max lt (200))
}
