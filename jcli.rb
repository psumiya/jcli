class Jcli < Formula
  desc "Sugared wrappers to execute methods from JDK and JVM-based libraries from the command line"
  homepage "https://github.com/psumiya/jcli"
  version "0.0.1-beta"

  if Hardware::CPU.intel?
    url "https://github.com/psumiya/jcli/releases/download/v0.0.1-beta/jcli-0.0.1-beta-osx-x86_64.tar.gz"
    sha256 "132de7b62340934215b84fa935b9258edd4d01fe32b105d59f04811fa9d655ee"
  else
    url "https://github.com/psumiya/jcli/releases/download/v0.0.1-beta/jcli-0.0.1-beta-osx-aarch64.tar.gz"
    sha256 "REPLACE_WITH_SHA256_OF_ARM64_TARBALL"
  end

  def install
    bin.install "bin/jcli"
  end

  test do
    assert_match "Usage: jcli", shell_output("#{bin}/jcli --help")
  end
end
