package whitelist

//import java.util.Base64
//
//object Java8Base64WhitelistUtil {
//  def encode(ipAddresses: String):String = Base64.getEncoder().encodeToString(ipAddresses.getBytes)
//
//  def decode(encodedIPAddresses: String):String = new String(Base64.getDecoder().decode(encodedIPAddresses),"utf-8")
//}
//
//object Test extends App{
//  // Write IP Addresses here to test encoding and decoding
//  // As PAAC service is in The Open. please remove IP Addresses once you test encode and decoding.
//  val ipAddresses:String = ""
//  val encodedIPAddresses =  Java8Base64WhitelistUtil.encode(ipAddresses)
//
//  println("Encoded IP Addresses = " + encodedIPAddresses)
//
//  val decodedIPAddresses =  Java8Base64WhitelistUtil.decode(encodedIPAddresses)
//
//  println("Decoded IP Addresses = " + decodedIPAddresses)
//
//  println(ipAddresses == decodedIPAddresses)
//}