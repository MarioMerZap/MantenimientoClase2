package org.mps.board;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdvertisementBoardTest {
    private AdvertisementBoard board;
    private AdvertiserDatabase advertiserDatabase;
    private PaymentGateway paymentGateway;

    @BeforeEach
    void setUp() {
        board = new AdvertisementBoard();
        advertiserDatabase = mock(AdvertiserDatabase.class);
        paymentGateway = mock(PaymentGateway.class);
    }

    @Test
    void testInitialAdvertisementExists() {
        assertEquals(1, board.numberOfPublishedAdvertisements());
    }

    @Test
    void testPublishByTheCompanyIncreasesCount() {
        Advertisement ad = new Advertisement("Promo", "Special offer", AdvertisementBoard.BOARD_OWNER);
        board.publish(ad, advertiserDatabase, paymentGateway);
        assertEquals(2, board.numberOfPublishedAdvertisements());
    }

    @Test
    void testPublishWithoutFundsFails() {
        Advertisement ad = new Advertisement("Discount", "Big sale", "Pepe Gotera y Otilio");
        when(advertiserDatabase.advertiserIsRegistered("Pepe Gotera y Otilio")).thenReturn(true);
        when(paymentGateway.advertiserHasFunds("Pepe Gotera y Otilio")).thenReturn(false);
        board.publish(ad, advertiserDatabase, paymentGateway);
        assertEquals(1, board.numberOfPublishedAdvertisements());
    }

    @Test
    void testPublishWithFundsSucceedsAndCharges() {
        Advertisement ad = new Advertisement("Tech Ad", "Latest gadget", "Robin Robot");
        when(advertiserDatabase.advertiserIsRegistered("Robin Robot")).thenReturn(true);
        when(paymentGateway.advertiserHasFunds("Robin Robot")).thenReturn(true);
        board.publish(ad, advertiserDatabase, paymentGateway);
        assertEquals(2, board.numberOfPublishedAdvertisements());
        verify(paymentGateway).chargeAdvertiser("Robin Robot");
    }

    @Test
    void testDeleteAdvertisement() {
        Advertisement ad1 = new Advertisement("Ad1", "Message1", AdvertisementBoard.BOARD_OWNER);
        Advertisement ad2 = new Advertisement("Ad2", "Message2", AdvertisementBoard.BOARD_OWNER);
        board.publish(ad1, advertiserDatabase, paymentGateway);
        board.publish(ad2, advertiserDatabase, paymentGateway);
        board.deleteAdvertisement("Ad1", AdvertisementBoard.BOARD_OWNER);
        assertFalse(board.findByTitle("Ad1").isPresent());
    }

    @Test
    void testNoDuplicateAdvertisements() {
        Advertisement ad = new Advertisement("Unique Ad", "Description", "Generic Advertiser");
        when(advertiserDatabase.advertiserIsRegistered("Generic Advertiser")).thenReturn(true);
        when(paymentGateway.advertiserHasFunds("Generic Advertiser")).thenReturn(true);
        board.publish(ad, advertiserDatabase, paymentGateway);
        board.publish(ad, advertiserDatabase, paymentGateway);
        assertEquals(2, board.numberOfPublishedAdvertisements());
    }

    @Test
    void testBoardFullThrowsException() {
        for (int i = 1; i < AdvertisementBoard.MAX_BOARD_SIZE; i++) {
            Advertisement ad = new Advertisement("Ad" + i, "Text", "Advertiser");
            when(advertiserDatabase.advertiserIsRegistered("Advertiser")).thenReturn(true);
            when(paymentGateway.advertiserHasFunds("Advertiser")).thenReturn(true);
            board.publish(ad, advertiserDatabase, paymentGateway);
        }
        Advertisement lastAd = new Advertisement("Final Ad", "Last spot", "Tim O'Theo");
        when(advertiserDatabase.advertiserIsRegistered("Tim O'Theo")).thenReturn(true);
        when(paymentGateway.advertiserHasFunds("Tim O'Theo")).thenReturn(true);
        assertThrows(AdvertisementBoardException.class, () -> board.publish(lastAd, advertiserDatabase, paymentGateway));
    }
}
