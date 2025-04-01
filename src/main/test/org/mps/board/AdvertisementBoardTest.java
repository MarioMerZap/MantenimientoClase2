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
        Advertisement ad = new Advertisement("Repair Services", "Best in town", "Pepe Gotera y Otilio");
        when(advertiserDatabase.exists("Pepe Gotera y Otilio")).thenReturn(true);
        when(paymentGateway.hasFunds("Pepe Gotera y Otilio")).thenReturn(false);
        
        board.publish(ad, advertiserDatabase, paymentGateway);
        assertEquals(1, board.numberOfPublishedAdvertisements());
    }

    @Test
    void testPublishWithFundsDeductsBalance() {
        Advertisement ad = new Advertisement("AI Services", "Future tech", "Robin Robot");
        when(advertiserDatabase.exists("Robin Robot")).thenReturn(true);
        when(paymentGateway.hasFunds("Robin Robot")).thenReturn(true);
        
        board.publish(ad, advertiserDatabase, paymentGateway);
        verify(paymentGateway).charge("Robin Robot");
    }

    @Test
    void testDeleteAdvertisementRemovesIt() {
        Advertisement ad1 = new Advertisement("Sale 1", "Big discounts", "THE Company");
        Advertisement ad2 = new Advertisement("Sale 2", "Even bigger discounts", "THE Company");
        
        board.publish(ad1, advertiserDatabase, paymentGateway);
        board.publish(ad2, advertiserDatabase, paymentGateway);
        board.removeAdvertisement(ad1);
        
        assertFalse(board.contains(ad1));
    }

    @Test
    void testDuplicateAdvertisementNotInserted() {
        Advertisement ad = new Advertisement("Exclusive Deal", "50% off", "Smart Advertiser");
        when(advertiserDatabase.exists("Smart Advertiser")).thenReturn(true);
        when(paymentGateway.hasFunds("Smart Advertiser")).thenReturn(true);
        
        board.publish(ad, advertiserDatabase, paymentGateway);
        board.publish(ad, advertiserDatabase, paymentGateway);
        
        assertEquals(2, board.numberOfPublishedAdvertisements());
    }

    @Test
    void testBoardFullThrowsException() {
        when(advertiserDatabase.exists("Tim O'Theo")).thenReturn(true);
        when(paymentGateway.hasFunds("Tim O'Theo")).thenReturn(true);
        
        for (int i = 0; i < AdvertisementBoard.MAX_ADS; i++) {
            board.publish(new Advertisement("Ad " + i, "Content", "Tim O'Theo"), advertiserDatabase, paymentGateway);
        }
        
        Advertisement extraAd = new Advertisement("Overflow Ad", "No space left", "Tim O'Theo");
        assertThrows(AdvertisementBoardException.class, () -> board.publish(extraAd, advertiserDatabase, paymentGateway));
    }
}
