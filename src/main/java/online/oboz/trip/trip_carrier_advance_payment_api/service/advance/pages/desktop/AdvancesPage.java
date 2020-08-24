package online.oboz.trip.trip_carrier_advance_payment_api.service.advance.pages.desktop;

import online.oboz.trip.trip_carrier_advance_payment_api.config.ApplicationProperties;
import online.oboz.trip.trip_carrier_advance_payment_api.service.advance.AdvanceService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.advance.tools.files.AttachmentService;
import online.oboz.trip.trip_carrier_advance_payment_api.service.util.ErrorUtils;
import online.oboz.trip.trip_carrier_advance_payment_api.service.util.SecurityUtils;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.AdvanceDesktopDTO;
import online.oboz.trip.trip_carrier_advance_payment_api.web.api.dto.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;


/**
 * Рабочий стол авансирования
 *
 * @author s‡udent
 */
@Service
public class AdvancesPage implements AdvanceDesktop {
    private static final Logger log = LoggerFactory.getLogger(AdvancesPage.class);
    private final AdvanceService advanceService;
    private final AttachmentService attachmentService;
    private final List<String> accessUsersEmails;


    @Autowired
    public AdvancesPage(
        AdvanceService advanceService,
        AttachmentService attachmentService,
        ApplicationProperties properties) {
        this.advanceService = advanceService;
        this.attachmentService = attachmentService;
        this.accessUsersEmails = properties.getAccessUsersEmails();
    }

    public ResponseEntity<AdvanceDesktopDTO> search(String tab, Filter filter) {
        checkAccess();
        return new ResponseEntity<>(advanceService.getAdvances(tab, filter), HttpStatus.OK);
    }

    @Transactional
    public ResponseEntity<Void> sendToUnfAdvance(Long id) {
        return advanceService.sendToUnfAdvance(id);
    }

    public ResponseEntity<Void> cancelAdvance(Long id, String comment) {
        return advanceService.cancelAdvance(id, comment);
    }

    public ResponseEntity<Void> setLoadingComplete(Long id, Boolean loadingComplete) {
        return advanceService.setLoadingComplete(id, loadingComplete);
    }

    public ResponseEntity<Void> changeComment(Long id, String comment) {
        return advanceService.changeComment(id, comment);
    }

    public ResponseEntity<Resource> downloadFile(UUID uuid) {
        log.info("[Аванс] Запрос на скачивание файла: {}. ", uuid);
        return attachmentService.fromBStore(uuid);
    }

    public ResponseEntity<Resource> getPdfPreview(UUID uuid, Integer pageNum) {
        log.info("[Аванс] Получить превью pdf-файла: {} - p.{}. ", uuid, pageNum);
        return attachmentService.getPdfPreview(uuid, pageNum);
    }


    public void checkAccess() {
        if (SecurityUtils.hasNotAccess(accessUsersEmails)) {
            throw ErrorUtils.authError("[Аванс]: Нарушение прав доступа к Рабочему столу.");
        }
    }

}
