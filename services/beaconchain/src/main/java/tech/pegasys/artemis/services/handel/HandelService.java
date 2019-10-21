package tech.pegasys.artemis.services.handel;

import com.google.common.eventbus.EventBus;
import io.libp2p.core.crypto.KeyKt;
import io.libp2p.core.crypto.PrivKey;
import org.apache.tuweni.bytes.Bytes;
import tech.pegasys.artemis.datastructures.Constants;
import tech.pegasys.artemis.networking.handel.libp2p.HandelP2PNetwork;
import tech.pegasys.artemis.networking.p2p.api.P2PNetwork;
import tech.pegasys.artemis.networking.p2p.jvmlibp2p.JvmLibp2pConfig;
import tech.pegasys.artemis.service.serviceutils.ServiceConfig;
import tech.pegasys.artemis.service.serviceutils.ServiceInterface;
import tech.pegasys.artemis.util.alogger.ALogger;

import java.util.Optional;
import tech.pegasys.artemis.util.time.Timer;
import tech.pegasys.artemis.util.time.TimerFactory;

public class HandelService implements ServiceInterface {

  private static final ALogger STDOUT = new ALogger(HandelService.class.getName());
  private P2PNetwork p2pNetwork;
  private EventBus eventBus;
    private Timer timer;

  public HandelService() {}

  @Override
  public void init(ServiceConfig config) {
    this.eventBus = config.getEventBus();
      this.eventBus.register(this);
      int timerPeriodInMiliseconds = (int) ((1.0 / Constants.TIME_TICKER_REFRESH_RATE) * 1000);
      try {
          this.timer =
              new TimerFactory()
                  .create(
                      config.getConfig().getTimer(),
                      new Object[] {this.eventBus, 0, timerPeriodInMiliseconds},
                      new Class[] {EventBus.class, Integer.class, Integer.class});
      } catch (IllegalArgumentException e) {
          System.exit(1);
      }

    Bytes bytes = Bytes.fromHexString(config.getConfig().getInteropPrivateKey());
    PrivKey pk = KeyKt.unmarshalPrivateKey(bytes.toArrayUnsafe());

    this.p2pNetwork =
        new HandelP2PNetwork(
            new JvmLibp2pConfig(
                Optional.of(pk),
                config.getConfig().getNetworkInterface(),
                config.getConfig().getPort(),
                config.getConfig().getAdvertisedPort(),
                config.getConfig().getStaticPeers(),
                true,
                true,
                true),
            eventBus);
  }


    @Override
    public void run() {
        // Start p2p adapter
        this.p2pNetwork.run();
    }

    @Override
    public void stop() {
        this.p2pNetwork.stop();
        this.timer.stop();
        this.eventBus.unregister(this);
    }

    P2PNetwork p2pNetwork() {
        return p2pNetwork;
    }
}
