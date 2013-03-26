package org.bouncycastle.crypto.tls.test;

import java.io.IOException;

import org.bouncycastle.crypto.tls.AlertDescription;
import org.bouncycastle.crypto.tls.CipherSuite;
import org.bouncycastle.crypto.tls.DefaultTlsServer;
import org.bouncycastle.crypto.tls.ProtocolVersion;
import org.bouncycastle.crypto.tls.TlsCredentials;
import org.bouncycastle.crypto.tls.TlsFatalAlert;

public class MockDTLSServer extends DefaultTlsServer {

    protected ProtocolVersion getMaximumVersion() {
        return ProtocolVersion.DTLSv10;
    }

    protected ProtocolVersion getMinimumVersion() {
        return ProtocolVersion.DTLSv10;
    }

    public TlsCredentials getCredentials() throws IOException {

        switch (selectedCipherSuite) {
        case CipherSuite.TLS_RSA_WITH_3DES_EDE_CBC_SHA:
            return TlsTestUtils.loadEncryptionCredentials(context, new String[] {
                "x509-server.pem", "x509-ca.pem" }, "x509-server-key.pem");

        default:
            /*
             * Note: internal error here; selected a key exchange we don't implement!
             */
            throw new TlsFatalAlert(AlertDescription.internal_error);
        }
    }
}