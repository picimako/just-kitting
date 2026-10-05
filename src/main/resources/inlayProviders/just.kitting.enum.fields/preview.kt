import com.intellij.microservices.http.HttpCode

class ApiChaosMode {
  companion object {
    private val responseCodes = setOf(
      HttpCode.BAD_GATEWAY/*<# 502 #>*/,
      HttpCode.GATEWAY_TIMEOUT/*<# 504 #>*/,
      /*...*/
    )
  }
}