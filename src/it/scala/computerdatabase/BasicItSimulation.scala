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

  val grpcConf = grpc(managedChannelBuilder(name = "localhost", port = 6000).usePlaintext())
    

  val request = grpc("store exchange request")
    .rpc(ExchangeServiceGrpc.METHOD_STORE_EXCHANGE)
    .payload(StoreExchangeRequest.defaultInstance.updateExpr(
                _.exchange.name :~ "test"
              ))


  val scn = scenario("Store Exchange")
    .exec(request)
  setUp(scn.inject(atOnceUsers(1)).protocols(grpcConf))
}
