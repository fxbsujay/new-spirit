package cn.spirit.go;

import cn.spirit.go.model.Room;
import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DisplayName("GameRoomService")
@ExtendWith(VertxExtension.class)
public class RoomTest {

    private static final Logger log = LoggerFactory.getLogger(RoomTest.class);

    @Test
    @DisplayName("禁入点测试A")
    void test1() {
        Room room = new Room(9);
        room.board[3][3] = Room.WHITE;
        room.board[2][4] = Room.WHITE;
        room.board[4][4] = Room.WHITE;
        room.board[3][4] = Room.BLACK;
        room.outPrintBoard();
        log.info("黑棋落子 4, 4, 结果={}",room.place(3, 5, Room.BLACK));
        room.outPrintBoard();
    }

    @Test
    @DisplayName("禁入点测试B")
    void test2() {
        Room room = new Room(9);
        room.board[3][3] = Room.WHITE;
        room.board[3][5] = Room.WHITE;
        room.board[2][4] = Room.WHITE;
        room.board[4][4] = Room.WHITE;
        room.outPrintBoard();
        log.info("黑棋落子 4, 4, 结果={}",room.place(3, 4, Room.BLACK));
        room.outPrintBoard();
    }


    @Test
    @DisplayName("提子测试")
    void test3() {
        Room room = new Room(9);
        room.board[3][3] = Room.WHITE;
        room.board[3][5] = Room.WHITE;
        room.board[2][4] = Room.WHITE;
        room.board[3][4] = Room.BLACK;
        room.outPrintBoard();
        log.info("白棋落子 4, 4, 结果={}",room.place(4, 4, Room.WHITE));
        room.outPrintBoard();
    }

}
