package src.service.Cart;

import src.service.Cart.Dto.CartCreateDto;
import src.service.Cart.Dto.CartDto;
import src.service.Cart.Dto.CartUpdateDto;
import src.service.IService;

public interface ICartService extends IService<CartDto, CartCreateDto, CartUpdateDto> {
}
