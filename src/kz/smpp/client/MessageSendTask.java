package kz.smpp.client;

import com.cloudhopper.commons.charset.CharsetUtil;
import com.cloudhopper.commons.util.windowing.WindowFuture;
import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.SmppSession;
import com.cloudhopper.smpp.pdu.*;
import com.cloudhopper.smpp.tlv.Tlv;
import com.cloudhopper.smpp.type.*;
import kz.smpp.mysql.MyDBConnection;
import kz.smpp.mysql.SmsLine;
import kz.smpp.utils.AllUtils;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class MessageSendTask implements Runnable {
    public static final org.slf4j.Logger log = LoggerFactory.getLogger(MessageSendTask.class);
    protected Client client;
    private MyDBConnection mDBConnection;


    public MessageSendTask(Client client, MyDBConnection mDBConn) {
        this.client = client;
        mDBConnection = new MyDBConnection();
    }

    @Override
    public void run() {
        if (!client.MessageSendTask) {
            client.MessageSendTask = true;
            if (client.state == ClientState.BOUND) {
                SmppSession session = client.getSession();
                List<SmsLine> SMs = mDBConnection.getSMSLine(0);
                for (SmsLine single_sm : SMs) {
                    //Последнее время активности системы
                    mDBConnection.setLastActivityTime();
                    try {
                        String source_address =mDBConnection.getSettings("my_msisdn");
                        if (single_sm.getServiceId()> 0)
                            source_address = mDBConnection.getContentTypeById(single_sm.getServiceId()).getService_code();

                        int SequenceNumber = 1 + (int) (Math.random() * 32000);
                        String client_msisdn = Long.toString(mDBConnection.getClient(single_sm.getId_client()).getAddrs());

                        byte[] textBytes = CharsetUtil.encode(single_sm.getSms_body(), "UCS-2");

                        SubmitSm sm = new  SubmitSm();
                        if (single_sm.getTransaction_id().length() > 0)
                            sm.setSourceAddress(new Address((byte) 0x00, (byte) 0x01, source_address.concat("#" + single_sm.getTransaction_id())));
                        else
                            sm.setSourceAddress(new Address((byte) 0x00, (byte) 0x01, source_address));
                        sm.setDestAddress(new Address((byte) 0x01, (byte) 0x01, client_msisdn));
                        sm.setDataCoding((byte) 8);
                        sm.setEsmClass((byte) 0);
                        sm.setShortMessage(null);
                        sm.setSequenceNumber(SequenceNumber);
                        sm.setOptionalParameter(new Tlv(SmppConstants.TAG_SOURCE_SUBADDRESS, mDBConnection.getSettings("0").getBytes(), "sourcesub_address"));
                        sm.setOptionalParameter(new Tlv(SmppConstants.TAG_MESSAGE_PAYLOAD, textBytes, "messagePayload"));
                        sm.calculateAndSetCommandLength();

                        log.debug("Send SM");

                        single_sm.setStatus(-1);
                        mDBConnection.UpdateSMSLine(single_sm);
                        //Указываем сразу ошибку отправки на случай неконтролируемого сбоя
                        if (!session.isClosed() && !session.isUnbinding()) {


                            SubmitSmResp resp = session.submit(sm, TimeUnit.SECONDS.toMillis(client.timeRespond));


                            if (resp.getCommandStatus() != 0) {
                                single_sm.setErr_code(Integer.toString(resp.getCommandStatus()));
                                single_sm.setStatus(-1);
                                mDBConnection.UpdateSMSLine(single_sm);
                            } else {
                                single_sm.setStatus(1);
                                mDBConnection.UpdateSMSLine(single_sm);
                            }
                        }
                    } catch (SmppTimeoutException | SmppChannelException
                            | UnrecoverablePduException | InterruptedException | RecoverablePduException ex) {
                        if (client.timeRespond < 60) client.timeRespond = client.timeRespond + 1;
                        log.debug("System's error, sending failure ", ex);
                    }
                }
            }
            client.MessageSendTask = false;
        }
    }
}
