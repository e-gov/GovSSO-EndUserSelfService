package ee.ria.govsso.enduserselfservice.actuator.health.certificates;

import ee.ria.govsso.enduserselfservice.BaseTest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.security.KeyStore;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@RequiredArgsConstructor(onConstructor_ = @Autowired)
class CertificateInfoLoaderTest extends BaseTest {

    private final KeyStore govssoSessionTrustStore;
    private final KeyStore igniteKeyStore;
    private final KeyStore igniteTrustStore;
    private final KeyStore taraTrustStore;

    @Test
    void loadCertificateInfos_govssoSessionTrustStore() {
        List<CertificateInfo> certificateInfos = CertificateInfoLoader.loadCertificateInfos(govssoSessionTrustStore);
        assertEquals(1, certificateInfos.size());
        CertificateInfo certificateInfo = certificateInfos.get(0);
        assertEquals("govsso-ca.localhost", certificateInfo.getAlias());
        assertEquals("CN=govsso-ca.localhost,O=govsso-local,L=Tallinn,C=EE", certificateInfo.getSubjectDN());
        assertNull(certificateInfo.getState());
        assertNull(certificateInfo.getWarning());
    }

    @Test
    void loadCertificateInfos_igniteKeyStore() {
        List<CertificateInfo> certificateInfos = CertificateInfoLoader.loadCertificateInfos(igniteKeyStore);
        assertEquals(1, certificateInfos.size());
        CertificateInfo certificateInfo = certificateInfos.get(0);
        assertEquals("enduserselfservice.localhost", certificateInfo.getAlias());
        assertEquals("CN=enduserselfservice.localhost", certificateInfo.getSubjectDN());
        assertNull(certificateInfo.getState());
        assertNull(certificateInfo.getWarning());
    }

    @Test
    void loadCertificateInfos_igniteTrustStore() {
        List<CertificateInfo> certificateInfos = CertificateInfoLoader.loadCertificateInfos(igniteTrustStore);
        assertEquals(1, certificateInfos.size());
        CertificateInfo certificateInfo = certificateInfos.get(0);
        assertEquals("govsso-ca.localhost", certificateInfo.getAlias());
        assertEquals("CN=govsso-ca.localhost,O=govsso-local,L=Tallinn,C=EE", certificateInfo.getSubjectDN());
        assertNull(certificateInfo.getState());
        assertNull(certificateInfo.getWarning());
    }

    @Test
    void loadCertificateInfos_taraTrustStore() {
        List<CertificateInfo> certificateInfos = CertificateInfoLoader.loadCertificateInfos(taraTrustStore);
        assertEquals(1, certificateInfos.size());
        CertificateInfo certificateInfo = certificateInfos.get(0);
        assertEquals("tara-ca.localhost", certificateInfo.getAlias());
        assertEquals("CN=tara-ca.localhost,O=tara-local,L=Tallinn,C=EE", certificateInfo.getSubjectDN());
        assertNull(certificateInfo.getState());
        assertNull(certificateInfo.getWarning());
    }
}
