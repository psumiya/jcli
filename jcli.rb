class Jcli < Formula
  desc "Sugared wrappers to execute methods from JDK and JVM-based libraries from the command line"
  homepage "https://github.com/psumiyapathak/jcli" # Assuming/Placeholder URL
  version "0.1"

  if Hardware::CPU.intel?
    url "https://github.com/psumiyapathak/jcli/releases/download/v0.1/jcli-0.1-osx-x86_64.tar.gz"
    sha256 "REPLACE_WITH_SHA256_OF_X86_64_TARBALL"
  else
    url "https://github.com/psumiyapathak/jcli/releases/download/v0.1/jcli-0.1-osx-aarch64.tar.gz"
    sha256 "REPLACE_WITH_SHA256_OF_ARM64_TARBALL"
  end

  def install
    bin.install "bin/jcli"
  end

  test do
    assert_match "Usage: jcli", shell_output("#{bin}/jcli --help")
  end
end
