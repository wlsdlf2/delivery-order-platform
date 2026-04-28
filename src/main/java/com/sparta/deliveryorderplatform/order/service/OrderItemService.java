package com.sparta.deliveryorderplatform.order.service;

import com.sparta.deliveryorderplatform.global.exception.CustomException;
import com.sparta.deliveryorderplatform.global.exception.ErrorCode;
import com.sparta.deliveryorderplatform.menu.entity.Menu;
import com.sparta.deliveryorderplatform.menu.repository.MenuRepository;
import com.sparta.deliveryorderplatform.order.dto.OrderItemRequestDto;
import com.sparta.deliveryorderplatform.order.dto.OrderRequestDto;
import com.sparta.deliveryorderplatform.order.entity.Order;
import com.sparta.deliveryorderplatform.order.entity.OrderItem;
import com.sparta.deliveryorderplatform.order.repository.OrderItemRepository;
import com.sparta.deliveryorderplatform.order.repository.OrderRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class OrderItemService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private MenuRepository menuRepository;

    /**
     * 주문 생성 시, 주문 메뉴 추가.
     * @param createOrder : 주문 메뉴 객체
     */
    @Transactional
    public void createOrderitem(OrderRequestDto createOrder, UUID orderId) {
        //주문 요청에서 주문 메뉴 리스트를 가져온다.
        List<OrderItemRequestDto> items = createOrder.getItems();

        //새로 저장된 Order를 가져온다.
        Order newOrder = orderRepository.findById(orderId).orElseThrow(()-> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        //Menu리스트를 가져와 map 형태로 만든다.
        List<UUID> menuIdList = items.stream().map(OrderItemRequestDto::getMenuId).toList();
        List<Menu> MenuList = menuRepository.findAllById(menuIdList);

        Map<UUID,Menu> menuMap = new HashMap<>();

        for(Menu menu : MenuList) {
            menuMap.put(menu.getMenuId(), menu);
        }

        //DB에 저장 시킬 createItems이다.
        List<OrderItem> createItems = new ArrayList<>();

        //items 수 만큼 반복하여
        for(OrderItemRequestDto item : items) {
            //개별적으로 Menu들을 뽑고
            Menu menu = menuMap.get(item.getMenuId());
            //createItems에 저장한다.
            createItems.add(OrderItem.createOrderItem(newOrder,menu, item.getQuantity(), menu.getPrice()));
        }

        //DB에 한번에 저장 시킨다.
        orderItemRepository.saveAll(createItems);
    }
}
